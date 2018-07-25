/**
 * Native Code Encryptor
 *
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */

#include <jni.h>
#include <cryptopp/aes.h>
#include <cryptopp/rc6.h>
#include <cryptopp/blowfish.h>
#include <cryptopp/twofish.h>
#include <cryptopp/gost.h>
#include <cryptopp/serpent.h>
#include <cryptopp/Threefish.h>
#include <cryptopp/shacal2.h>
#include <cryptopp/modes.h>
#include <cryptopp/filters.h>

using namespace std;
using namespace CryptoPP;


extern "C" {
	JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_encryptByteArrayNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode);
	JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_decryptByteArrayNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode);

	JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_decryptByteArrayCTRNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode);
	JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_encryptByteArrayCTRNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode);
};

JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_encryptByteArrayNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode)
{
	int ok = 0;

	try {
		jsize ivSize = env->GetArrayLength(ivX);
		jbyte *iv = (jbyte *)env->GetByteArrayElements(ivX, 0);
		jsize keySize = env->GetArrayLength(keyX);
		jbyte *key = (jbyte *)env->GetByteArrayElements(keyX, 0);
		jsize dataSize = env->GetArrayLength(dataX);
		jbyte *data = (jbyte *)env->GetByteArrayElements(dataX, 0);

		switch (algorithmCode)
		{
			case 0:
			{
				CryptoPP::CBC_Mode<AES>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 1:
			{
				CryptoPP::CBC_Mode<RC6>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 2:
			{
				CryptoPP::Serpent::setModeCode(0);
				CryptoPP::CBC_Mode<Serpent>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 3:
			{
				CryptoPP::CBC_Mode<Blowfish>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 4:
			{
				CryptoPP::CBC_Mode<Twofish>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 5:
			{
				CryptoPP::CBC_Mode<GOST>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 6:
			{
				CryptoPP::CBC_Mode<Blowfish>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 7:
			{
				CryptoPP::CBC_Mode<Threefish_1024>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 8:
			{
				CryptoPP::CBC_Mode<SHACAL2>::Encryption cbcEncryption((byte*)key, keySize, (byte*)iv);
				cbcEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
		}

		env->ReleaseByteArrayElements(ivX, iv, JNI_ABORT);
		env->ReleaseByteArrayElements(keyX, key, JNI_ABORT);
		env->ReleaseByteArrayElements(dataX, data, 0);

	} catch (...) {} // swallow (ok = 0)

    return ok;
}

JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_decryptByteArrayNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode)
{
	int ok = 0;

	try {
		jsize ivSize = env->GetArrayLength(ivX);
		jbyte *iv = (jbyte *)env->GetByteArrayElements(ivX, 0);
		jsize keySize = env->GetArrayLength(keyX);
		jbyte *key = (jbyte *)env->GetByteArrayElements(keyX, 0);
		jsize dataSize = env->GetArrayLength(dataX);
		jbyte *data = (jbyte *)env->GetByteArrayElements(dataX, 0);

		switch (algorithmCode)
		{
			case 0:
			{
				CryptoPP::CBC_Mode<AES>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 1:
			{
				CryptoPP::CBC_Mode<RC6>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 2:
			{
				CryptoPP::Serpent::setModeCode(0);
				CryptoPP::CBC_Mode<Serpent>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 3:
			{
				CryptoPP::CBC_Mode<Blowfish>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 4:
			{
				CryptoPP::CBC_Mode<Twofish>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 5:
			{
				CryptoPP::CBC_Mode<GOST>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 6:
			{
				CryptoPP::CBC_Mode<Blowfish>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 7:
			{
				CryptoPP::CBC_Mode<Threefish_1024>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 8:
			{
				CryptoPP::CBC_Mode<SHACAL2>::Decryption cbcDecryption((byte*)key, keySize, (byte*)iv);
				cbcDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
		}

		env->ReleaseByteArrayElements(ivX, iv, JNI_ABORT);
		env->ReleaseByteArrayElements(keyX, key, JNI_ABORT);
		env->ReleaseByteArrayElements(dataX, data, 0);

	} catch (...) {} // swallow (ok = 0)

    return ok;
}

JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_encryptByteArrayCTRNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode)
{
	int ok = 0;

	try {
		jsize ivSize = env->GetArrayLength(ivX);
		jbyte *iv = (jbyte *)env->GetByteArrayElements(ivX, 0);
		jsize keySize = env->GetArrayLength(keyX);
		jbyte *key = (jbyte *)env->GetByteArrayElements(keyX, 0);
		jsize dataSize = env->GetArrayLength(dataX);
		jbyte *data = (jbyte *)env->GetByteArrayElements(dataX, 0);

		switch (algorithmCode)
		{
			case 0:
			{
				CryptoPP::CTR_Mode<AES>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 1:
			{
				CryptoPP::CTR_Mode<RC6>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 2:
			{
				CryptoPP::Serpent::setModeCode(1);
				CryptoPP::CTR_Mode<Serpent>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 3:
			{
				CryptoPP::CTR_Mode<Blowfish>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 4:
			{
				CryptoPP::CTR_Mode<Twofish>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 5:
			{
				CryptoPP::CTR_Mode<GOST>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 6:
			{
				CryptoPP::CTR_Mode<Blowfish>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 7:
			{
				CryptoPP::CTR_Mode<Threefish_1024>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 8:
			{
				CryptoPP::CTR_Mode<SHACAL2>::Encryption ctrEncryption((byte*)key, keySize, (byte*)iv);
				ctrEncryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
		}

		env->ReleaseByteArrayElements(ivX, iv, JNI_ABORT);
		env->ReleaseByteArrayElements(keyX, key, JNI_ABORT);
		env->ReleaseByteArrayElements(dataX, data, 0);

	} catch (...) {} // swallow (ok = 0)

    return ok;
}

JNIEXPORT jint JNICALL Java_com_paranoiaworks_unicus_android_sse_nativecode_EncryptorNC_decryptByteArrayCTRNC (JNIEnv *env, jobject jobj, jbyteArray ivX, jbyteArray keyX, jbyteArray dataX, jint algorithmCode)
{
	int ok = 0;

	try {
		jsize ivSize = env->GetArrayLength(ivX);
		jbyte *iv = (jbyte *)env->GetByteArrayElements(ivX, 0);
		jsize keySize = env->GetArrayLength(keyX);
		jbyte *key = (jbyte *)env->GetByteArrayElements(keyX, 0);
		jsize dataSize = env->GetArrayLength(dataX);
		jbyte *data = (jbyte *)env->GetByteArrayElements(dataX, 0);

		switch (algorithmCode)
		{
			case 0:
			{
				CryptoPP::CTR_Mode<AES>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 1:
			{
				CryptoPP::CTR_Mode<RC6>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 2:
			{
				CryptoPP::Serpent::setModeCode(1);
				CryptoPP::CTR_Mode<Serpent>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 3:
			{
				CryptoPP::CTR_Mode<Blowfish>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 4:
			{
				CryptoPP::CTR_Mode<Twofish>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 5:
			{
				CryptoPP::CTR_Mode<GOST>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 6:
			{
				CryptoPP::CTR_Mode<Blowfish>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 7:
			{
				CryptoPP::CTR_Mode<Threefish_1024>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
			case 8:
			{
				CryptoPP::CTR_Mode<SHACAL2>::Decryption ctrDecryption((byte*)key, keySize, (byte*)iv);
				ctrDecryption.ProcessData((byte*) data, (byte*) data, dataSize);
				ok = 1;
				break;
			}
		}

		env->ReleaseByteArrayElements(ivX, iv, JNI_ABORT);
		env->ReleaseByteArrayElements(keyX, key, JNI_ABORT);
		env->ReleaseByteArrayElements(dataX, data, 0);

	} catch (...) {} // swallow (ok = 0)

    return ok;
}
