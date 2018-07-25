LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := cryptopp_static

FILE_LIST_CX := $(wildcard $(LOCAL_PATH)/*.c)
FILE_LIST_CPPX := $(wildcard $(LOCAL_PATH)/*.cpp)

LOCAL_SRC_FILES :=\
        $(FILE_LIST_CX:$(LOCAL_PATH)/%=%) \
        $(FILE_LIST_CPPX:$(LOCAL_PATH)/%=%)
        
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES := $(LOCAL_EXPORT_C_INCLUDES)
       
include $(BUILD_STATIC_LIBRARY)
