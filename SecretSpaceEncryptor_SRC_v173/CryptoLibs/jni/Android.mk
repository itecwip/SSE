LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE := scrypt

FILE_LIST_C := $(wildcard $(LOCAL_PATH)/*.c)

LOCAL_SRC_FILES :=\
        $(FILE_LIST_C:$(LOCAL_PATH)/%=%) \

include $(BUILD_SHARED_LIBRARY)

