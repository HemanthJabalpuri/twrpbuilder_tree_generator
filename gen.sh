#!/bin/sh

# TWRP device tree generator
# by HemanthJabalpuri @ XDA
# 
# Credits:-
#  - https://github.com/twrpdtgen/twrpdtgen
#  - https://github.com/TwrpBuilder/twrpbuilder_tree_generator

if [ $# -ne 4 ]; then
  echo "Usage: gen.sh recovery.img props.txt AIKfolderpath treedir"
  exit
fi

#set -x

#################
recoveryfile="$1"
buildprop="$2"
aikhome="$3"
treedir="$4"
#################

tmpd="$PWD"; [ "$PWD" = "/" ] && tmpd=""
case "$0" in
  /*) cdir="$0";;
  *) cdir="$tmpd/${0#./}";;
esac
cdir="${cdir%/*}"

cleanupAIK() {
  ( cd "$aikhome"
  chmod 777 cleanup.sh
  ./cleanup.sh )
}

cleanupAIK

cd "$aikhome"
chmod 777 unpackimg.sh
./unpackimg.sh "$recoveryfile"

cd "$treedir"

stripExtras() {
  echo "$1" | tr ' [:punct:]' '_'
}

get_prop() {

  #TODO: get prop from ramdisk if present instead from getprop output taken from system

  grep $1 "$buildprop" | head -n 1 | cut -d \[ -f 3 | cut -d \] -f 1
}


genomnimk() {
  local manufacturer="$(get_prop ro.product.manufacturer)"
  local brand="$(stripExtras "$(get_prop ro.product.brand)")"
  local model="$(get_prop ro.product.model)"
  device="$(stripExtras "$(get_prop ro.product.device)")"
  devpath="device/$brand/$device"

  mkdir -p "$devpath"
  cd "$devpath"
  {
  echo '$(call inherit-product, $(SRC_TARGET_DIR)/product/base.mk)'
  echo '$(call inherit-product, vendor/omni/config/common.mk)'
  echo ''
  echo "\$(call inherit-product, $devpath/device.mk)"
  echo ''
  echo "PRODUCT_DEVICE := $device"
  echo "PRODUCT_NAME := omni_$device"
  echo "PRODUCT_BRAND := $brand"
  echo "PRODUCT_MODEL := $model"
  echo "PRODUCT_MANUFACTURER := $manufacturer"
  } > "omni_${device}.mk"
}


gendevicemk() {
  shippingapi=$(get_prop ro.product.first_api_level)
  dynamic=$(get_prop ro.boot.dynamic_partitions)
  echo "LOCAL_PATH := $devpath" > device.mk
  if ! [ -z "$shippingapi" ]; then
    echo "PRODUCT_SHIPPING_API_LEVEL := $shippingapi" >> device.mk
  fi
  if [ "$dynamic" = "true" ]; then
    echo 'PRODUCT_USE_DYNAMIC_PARTITIONS := true' >> device.mk
  fi
}


genAndroidmk() {
  {
  echo 'LOCAL_PATH := $(call my-dir)'
  echo "ifeq (\$(TARGET_DEVICE),${device})"
  echo 'include $(call all-subdir-makefiles,$(LOCAL_PATH))'
  echo 'endif'
  } > Android.mk
}


genAndroidProductsmk() {
  {
  echo "LOCAL_PATH := $devpath"
  echo "PRODUCT_MAKEFILES := \$(LOCAL_PATH)/omni_${device}.mk"
  } > AndroidProducts.mk
}


genPrebuilts() {
  mkdir prebuilt
  cp "$aikhome/split_img/*-kernel" prebuilt/kernel
  if ls "$aikhome/split_img" | grep -q 'dtb$'; then
    prebuiltdtb=1
    mkdir prebuilt/dtb
    cp "$aikhome/split_img/*-dtb" prebuilt/dtb/prebuilt.dtb
  fi
  if ls "$aikhome/split_img" | grep -q 'dtbo$'; then
    prebuiltdtbo=1
    cp "$aikhome/split_img/*dtbo" prebuilt/dtbo.img
  fi
  if ls "$aikhome/split_img" | grep -q 'dt$'; then
    prebuiltdt=1
    cp "$aikhome/split_img/*-dt" prebuilt/dt.img
  fi
}


grep_part() {
  local part="$(grep "$1" "$fstab")"
  echo "$part" | while read line; do
    case "$line" in
      '#'*) continue;;
    esac
    ! [ -z "$line" ] && echo "$line" && return
  done
}

genfstab() {
  if [ -f "$aikhome/ramdisk/etc/recovery.fstab" ]; then
    fstab="$aikhome/ramdisk/etc/recovery.fstab"
  elif [ -f "$aikhome/ramdisk/system/etc/recovery.fstab" ]; then
    fstab="$aikhome/ramdisk/system/etc/recovery.fstab"
  else
    return
  fi

  {
  grep_part '/system'
  grep_part '/vendor'
  } > recovery.fstab

  if [ "$dynamic" = "true" ]; then
    {
    grep_part '^odm'
    grep_part '^system_ext'
    grep_part '^product'
    } >> recovery.fstab
  fi

  {
  echo ""
  grep_part '/metadata'
  grep_part '/data'
  grep_part '/cache'
  grep_part '/persist'
  grep_part '/misc'
  grep_part ' /boot '
  grep_part '/recovery'
  grep_part '/dtbo'
  grep_part '/vbmeta'
  } >> recovery.fstab
}


genKernelConfigs() {
  cd "$aikhome/split_img"

  echo '# Kernel'
  if ls | grep -q 'cmdline$'; then
    local cmdline="$(cat ./*-cmdline)"
    echo "BOARD_KERNEL_CMDLINE := $cmdline"
  fi
  if ls | grep -q 'header_version$'; then
    local headerver="$(cat ./*-header_version)"
    echo "BOARD_BOOTIMG_HEADER_VERSION := $headerver"
    echo 'BOARD_MKBOOTIMG_ARGS += --header_version $(BOARD_BOOTIMG_HEADER_VERSION)'
  fi
  if ls | grep -q 'base$'; then
    local base="$(cat ./*-base)"
    echo "BOARD_KERNEL_BASE := $base"
  fi
  if ls | grep -q 'pagesize$'; then
    local pagesize="$(cat ./*-pagesize)"
    echo "BOARD_KERNEL_PAGESIZE := $pagesize"
    echo "BOARD_FLASH_BLOCK_SIZE := $((pagesize*64)) # (BOARD_KERNEL_PAGESIZE * 64)"
  fi
  if ls | grep -q 'ramdisk_offset$'; then
    local ramdisk_offset="$(cat ./*-ramdisk_offset)"
    echo "BOARD_RAMDISK_OFFSET := $ramdisk_offset"
    echo 'BOARD_MKBOOTIMG_ARGS += --ramdisk_offset $(BOARD_RAMDISK_OFFSET)'
  fi
  if ls | grep -q 'tags_offset$'; then
    local tags_offset="$(cat ./*-tags_offset)"
    echo "BOARD_KERNEL_TAGS_OFFSET := $tags_offset"
    echo 'BOARD_MKBOOTIMG_ARGS += --tags_offset $(BOARD_KERNEL_TAGS_OFFSET)'
  fi
  if [ "$prebuiltdt" = "1" ]; then
    echo 'TARGET_PREBUILT_DT := $(DEVICE_PATH)/prebuilt/dt.img'
    echo 'BOARD_MKBOOTIMG_ARGS += --dt $(TARGET_PREBUILT_DT)'
  fi
  if [ "$prebuiltdtb" = "1" ]; then
    echo 'BOARD_INCLUDE_DTB_IN_BOOTIMG := true'
    echo 'BOARD_PREBUILT_DTBIMAGE_DIR := $(DEVICE_PATH)/prebuilt/dtb'
  fi
  if [ "$prebuiltdtbo" = "1" ]; then
    echo 'BOARD_INCLUDE_RECOVERY_DTBO := true'
    echo 'BOARD_PREBUILT_DTBOIMAGE := $(DEVICE_PATH)/prebuilt/dtbo.img'
  fi
  echo 'TARGET_PREBUILT_KERNEL := $(DEVICE_PATH)/prebuilt/kernel'

  if ls | grep -q 'ramdiskcomp$'; then
    local ramdiskcomp="$(cat ./*-ramdiskcomp)"
    if [ "$ramdiskcomp" = "lzma" ]; then
      echo ''
      echo '# Ramdisk compression'
      echo 'LZMA_RAMDISK_TARGETS := recovery'
    fi
  fi

  echo ''
  cd - >/dev/null
}


genBoardConfig() {
  local arch, platform, board
#  platform=$(get_prop ro.mediatek.platform)
#  [ "$platform" ] || platform=generic
  arch=arm
  if get_prop ro.product.cpu.abi | grep -q arm64-v8a; then
    arch=arm64-v8a
  fi

  {
  echo "DEVICE_PATH := $devpath"
  echo ''

  echo '# Architecture'
  if [ "$arch" = "arm64-v8a" ]; then
    echo 'TARGET_ARCH := arm64'
    echo 'TARGET_ARCH_VARIANT := armv8-a'
    echo 'TARGET_CPU_ABI := arm64-v8a'
    echo 'TARGET_CPU_ABI2 :='
    echo 'TARGET_CPU_VARIANT := generic'
    echo ''
    echo 'TARGET_2ND_ARCH := arm'
    echo 'TARGET_2ND_ARCH_VARIANT := armv7-a-neon'
    echo 'TARGET_2ND_CPU_ABI := armeabi-v7a'
    echo 'TARGET_2ND_CPU_ABI2 := armeabi'
    echo 'TARGET_2ND_CPU_VARIANT := generic'
    echo 'TARGET_BOARD_SUFFIX := _64'
    echo 'TARGET_USES_64_BIT_BINDER := true'
  elif [ "$arch" = "arm" ]; then
    echo 'TARGET_ARCH := arm'
    echo 'TARGET_ARCH_VARIANT := armv7-a-neon'
    echo 'TARGET_CPU_ABI := armeabi-v7a'
    echo 'TARGET_CPU_ABI2 := armeabi'
    echo 'TARGET_CPU_VARIANT := generic'
  fi
  echo ''

  platform="$(get_prop ro.board.platform)"
#  board="$(get_prop ro.product.board)"
  if ls "$aikhome/split_img" | grep -q 'board$'; then
    board="$(cat "$aikhome/split_img/*-board")"
  fi
  echo "TARGET_BOARD_PLATFORM := $platform"
  echo "TARGET_BOOTLOADER_BOARD_NAME := $board"
#  echo 'TARGET_NO_BOOTLOADER := true'
  echo ''

  genKernelConfigs

  echo 'BOARD_HAS_LARGE_FILESYSTEM := true'
#  echo 'BOARD_HAS_NO_SELECT_BUTTON := true'

  local recovery_size="$(stat -c %s "$recoveryfile")"
  if [ "$((recovery_size % 2097152))" -eq 0 ]; then
    if ! [ -z "$shippingapi" ] && [ "$shippingapi" -ge 26 ]; then
      echo '# Android Verified Boot'
      echo 'BOARD_AVB_ENABLE := true'
      if [ "$dynamic" = "true" ]; then
        echo 'BOARD_AVB_RECOVERY_ALGORITHM := SHA256_RSA2048'
        echo 'BOARD_AVB_RECOVERY_KEY_PATH := external/avb/test/data/testkey_rsa2048.pem'
        echo 'BOARD_AVB_RECOVERY_ROLLBACK_INDEX := 1'
        echo 'BOARD_AVB_RECOVERY_ROLLBACK_INDEX_LOCATION := 1'
      else
        echo 'BOARD_AVB_ROLLBACK_INDEX := $(PLATFORM_SECURITY_PATCH_TIMESTAMP)'
      fi
      echo ''
    fi
    echo "BOARD_BOOTIMAGE_PARTITION_SIZE := $recovery_size"
    echo "BOARD_RECOVERYIMAGE_PARTITION_SIZE := $recovery_size # This is the maximum known partition size, but it can be higher"
  else
    echo "#BOARD_RECOVERYIMAGE_PARTITION_SIZE := $recovery_size # This is the maximum known partition size, but it can be higher, so we just omit it"
  fi

  echo 'BOARD_SYSTEMIMAGE_PARTITION_TYPE := ext4'
  echo 'BOARD_USERDATAIMAGE_FILE_SYSTEM_TYPE := ext4'
  echo 'BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE := ext4'
  echo 'TARGET_USERIMAGES_USE_EXT4 := true'
  echo 'TARGET_USERIMAGES_USE_F2FS := true'
  echo 'TARGET_COPY_OUT_VENDOR := vendor'
  echo ''
  echo '# Hack: prevent anti rollback'
  echo 'PLATFORM_SECURITY_PATCH := 2099-12-31'
  echo 'VENDOR_SECURITY_PATCH := 2099-12-31'
  echo 'PLATFORM_VERSION := 16.1.0'
  echo ''
  echo '# TWRP Configuration'
  echo 'TW_THEME := portrait_hdpi'
  echo 'TW_EXTRA_LANGUAGES := true'
  echo 'TW_SCREEN_BLANK_ON_BOOT := true'
  echo 'TW_INPUT_BLACKLIST := "hbtp_vm"'
  echo 'TW_USE_TOOLBOX := true'
#  echo 'RECOVERY_SDCARD_ON_DATA := true'
  } > BoardConfig.mk
}

main() {
  echo "Generating omni.mk"
  genomnimk

  echo "Generating device.mk"
  gendevicemk

  echo "Generating Android.mk"
  genAndroidmk

  echo "Generating AndroidProducts.mk"
  genAndroidProductsmk

  echo "Generating prebuilts"
  genPrebuilts

  echo "Generating fstab"
  genfstab

  echo "Generating BoardConfig.mk"
  genBoardConfig

  cleanupAIK

  if [ "$dynamic" = "true" ]; then
    # use twrp-11 manifest for devices having dynamic partitions
    mv "omni_${device}.mk" "twrp_${device}.mk"
    sed -i 's/omni/twrp/g' "twrp_${device}.mk"
    sed -i 's/omni/twrp/g' AndroidProducts.mk
  fi
}

main
