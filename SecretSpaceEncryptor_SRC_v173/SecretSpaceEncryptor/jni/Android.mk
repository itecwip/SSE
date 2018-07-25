LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE := pwncenc

FILE_LIST_C := $(wildcard $(LOCAL_PATH)/*.c)
FILE_LIST_CPP := $(wildcard $(LOCAL_PATH)/*.cpp)
#FILE_LIST_X := $(wildcard $(LOCAL_PATH)/cryptopp/*.c)
#FILE_LIST_XX := $(wildcard $(LOCAL_PATH)/cryptopp/*.cpp)

LOCAL_SRC_FILES :=\
        $(FILE_LIST_C:$(LOCAL_PATH)/%=%) \
        $(FILE_LIST_CPP:$(LOCAL_PATH)/%=%) \
        #$(FILE_LIST_X:$(LOCAL_PATH)/%=%) \
        #$(FILE_LIST_XX:$(LOCAL_PATH)/%=%) \

LOCAL_STATIC_LIBRARIES := cryptopp_static
 
include $(BUILD_SHARED_LIBRARY)

$(call import-module,cryptopp)

