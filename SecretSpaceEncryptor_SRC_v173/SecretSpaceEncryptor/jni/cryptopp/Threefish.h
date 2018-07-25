#ifndef CRYPTOPP_THREEFISH_H
#define CRYPTOPP_THREEFISH_H

#include "seckey.h"
#include "secblock.h"

NAMESPACE_BEGIN(CryptoPP)

// "key" = Key || Tweak
struct Threefish256_Info : public FixedKeyLength<48>, public FixedBlockSize<32>, public FixedRounds<72>
{
	static const char* StaticAlgorithmName() {return "Threefish-256";}
};

// "key" = Key || Tweak
struct Threefish512_Info : public FixedKeyLength<80>, public FixedBlockSize<64>, public FixedRounds<72>
{
	static const char* StaticAlgorithmName() {return "Threefish-512";}
};

// "key" = Key || Tweak
struct Threefish1024_Info : public FixedKeyLength<144>, public FixedBlockSize<128>, public FixedRounds<80>
{
	static const char* StaticAlgorithmName() {return "Threefish-1024";}
};

class Threefish_256 : public Threefish256_Info, public BlockCipherDocumentation
{
	class CRYPTOPP_NO_VTABLE Base : public BlockCipherImpl<Threefish256_Info>
	{
	public:
		void UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs &params);

	protected:
		FixedSizeSecBlock<word64, 5> m_key;
		FixedSizeSecBlock<word64, 3> m_tweak;
	};
	class CRYPTOPP_NO_VTABLE Enc : public Base
	{
		void ThreefishEncrypt256(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
	class CRYPTOPP_NO_VTABLE Dec : public Base
	{
		void ThreefishDecrypt256(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
public:
	typedef BlockCipherFinal<ENCRYPTION,Enc> Encryption;
	typedef BlockCipherFinal<DECRYPTION,Dec> Decryption;
};

class Threefish_512 : public Threefish512_Info, public BlockCipherDocumentation
{
	class CRYPTOPP_NO_VTABLE Base : public BlockCipherImpl<Threefish512_Info>
	{
	public:
		void UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs &params);

	protected:
		FixedSizeSecBlock<word64, 9> m_key;
		FixedSizeSecBlock<word64, 3> m_tweak;
	};
	class CRYPTOPP_NO_VTABLE Enc : public Base
	{
		void ThreefishEncrypt512(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
	class CRYPTOPP_NO_VTABLE Dec : public Base
	{
		void ThreefishDecrypt512(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
public:
	typedef BlockCipherFinal<ENCRYPTION,Enc> Encryption;
	typedef BlockCipherFinal<DECRYPTION,Dec> Decryption;
};

class Threefish_1024 : public Threefish1024_Info, public BlockCipherDocumentation
{
	class CRYPTOPP_NO_VTABLE Base : public BlockCipherImpl<Threefish1024_Info>
	{
	public:
		void UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs &params);

	protected:
		FixedSizeSecBlock<word64, 17> m_key;
		FixedSizeSecBlock<word64, 3> m_tweak;
	};
	class CRYPTOPP_NO_VTABLE Enc : public Base
	{
		void ThreefishEncrypt1024(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
	class CRYPTOPP_NO_VTABLE Dec : public Base
	{
		void ThreefishDecrypt1024(word64* input, word64* output) const;
	public:
		void ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const;
	};
public:
	typedef BlockCipherFinal<ENCRYPTION,Enc> Encryption;
	typedef BlockCipherFinal<DECRYPTION,Dec> Decryption;
};

NAMESPACE_END

#endif