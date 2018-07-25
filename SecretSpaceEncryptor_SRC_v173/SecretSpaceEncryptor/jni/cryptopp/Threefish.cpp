// Threefish.cpp - written and placed in the public domain by Jean-Pierre Muench
// using the code from Skein3Fish


#include "pch.h"
#include "Threefish.h"
#include "misc.h"

#define KeyScheduleConst 0x1BD11BDAA9FC1A22L

NAMESPACE_BEGIN(CryptoPP)

typedef BlockGetAndPut<word64,LittleEndian> Block;
typedef GetBlock<word64,LittleEndian> CurrentGetBlock;

void Threefish_256::Base::UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs&)
{
	AssertValidKeyLength(length);
	const int keyWords = (length / 8)-2;
	word64 parity = KeyScheduleConst;

	const word64* ConvertedKey = (word64*) userKey;

	m_tweak[0] = ConvertedKey[keyWords];
	m_tweak[1] = ConvertedKey[keyWords+1];
	m_tweak[2] = m_tweak[0] ^ m_tweak[1];

	for (int i = 0; i < keyWords; i++) {
		m_key[i] = ConvertedKey[i];
		parity ^= ConvertedKey[i];
	}
	m_key[keyWords] = parity;
}

void Threefish_256::Enc::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	CurrentGetBlock Buffer0 = Block::Get(inBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer0 = Buffer0(plainBlock[i]);

	ThreefishEncrypt256(plainBlock,cipherBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(cipherBlock[i]);
}

void Threefish_256::Dec::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	CurrentGetBlock Buffer = Block::Get(inBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer = Buffer(cipherBlock[i]);
	//Block::Get(inBlock)(cipherBlock[0])(cipherBlock[1])(cipherBlock[2])(cipherBlock[3]);

	ThreefishDecrypt256(cipherBlock,plainBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(plainBlock[i]);
	//Block::Put(xorBlock,outBlock)(plainBlock[0])(plainBlock[1])(plainBlock[2])(plainBlock[3]);
}

void Threefish_512::Base::UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs&)
{
	AssertValidKeyLength(length);
	const int keyWords = (length / 8)-2;
	word64 parity = KeyScheduleConst;

	const word64* ConvertedKey = (word64*) userKey;

	m_tweak[0] = ConvertedKey[keyWords];
	m_tweak[1] = ConvertedKey[keyWords+1];
	m_tweak[2] = m_tweak[0] ^ m_tweak[1];

	for (int i = 0; i < keyWords; i++) {
		m_key[i] = ConvertedKey[i];
		parity ^= ConvertedKey[i];
	}
	m_key[keyWords] = parity;
}

void Threefish_512::Enc::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	//Block::Get(inBlock)(plainBlock[0])(plainBlock[1])(plainBlock[2])(plainBlock[3])(plainBlock[4])(plainBlock[5])(plainBlock[6])(plainBlock[7]);
	CurrentGetBlock Buffer0 = Block::Get(inBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer0 = Buffer0(plainBlock[i]);

	ThreefishEncrypt512(plainBlock,cipherBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(cipherBlock[i]);
	//Block::Put(xorBlock,outBlock)(cipherBlock[0])(cipherBlock[1])(cipherBlock[2])(cipherBlock[3])(cipherBlock[4])(cipherBlock[5])(cipherBlock[6])(cipherBlock[7]);
}

void Threefish_512::Dec::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	CurrentGetBlock Buffer = Block::Get(inBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer = Buffer(cipherBlock[i]);
	//Block::Get(inBlock)(cipherBlock[0])(cipherBlock[1])(cipherBlock[2])(cipherBlock[3])(cipherBlock[4])(cipherBlock[5])(cipherBlock[6])(cipherBlock[7]);

	ThreefishDecrypt512(cipherBlock,plainBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(plainBlock[i]);
	//Block::Put(xorBlock,outBlock)(plainBlock[0])(plainBlock[1])(plainBlock[2])(plainBlock[3])(plainBlock[4])(plainBlock[5])(plainBlock[6])(plainBlock[7]);
}

void Threefish_1024::Base::UncheckedSetKey(const byte *userKey, unsigned int length, const NameValuePairs&)
{
	AssertValidKeyLength(length);
	const int keyWords = (length / 8)-2;
	word64 parity = KeyScheduleConst;

	const word64* ConvertedKey = (word64*) userKey;

	m_tweak[0] = ConvertedKey[keyWords];
	m_tweak[1] = ConvertedKey[keyWords+1];
	m_tweak[2] = m_tweak[0] ^ m_tweak[1];

	for (int i = 0; i < keyWords; i++) {
		m_key[i] = ConvertedKey[i];
		parity ^= ConvertedKey[i];
	}
	m_key[keyWords] = parity;
}

void Threefish_1024::Enc::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	//Block::Get(inBlock)(plainBlock[0])(plainBlock[1])(plainBlock[2])(plainBlock[3])(plainBlock[4])(plainBlock[5])(plainBlock[6])(plainBlock[7]);
	CurrentGetBlock Buffer0 = Block::Get(inBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer0 = Buffer0(plainBlock[i]);

	ThreefishEncrypt1024(plainBlock,cipherBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(cipherBlock[i]);
	//Block::Put(xorBlock,outBlock)(cipherBlock[0])(cipherBlock[1])(cipherBlock[2])(cipherBlock[3])(cipherBlock[4])(cipherBlock[5])(cipherBlock[6])(cipherBlock[7]);
}

void Threefish_1024::Dec::ProcessAndXorBlock(const byte *inBlock, const byte *xorBlock, byte *outBlock) const
{
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> cipherBlock;
	FixedSizeSecBlock<word64,(BLOCKSIZE/8)> plainBlock;

	CurrentGetBlock Buffer = Block::Get(inBlock)(cipherBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer = Buffer(cipherBlock[i]);
	//Block::Get(inBlock)(cipherBlock[0])(cipherBlock[1])(cipherBlock[2])(cipherBlock[3])(cipherBlock[4])(cipherBlock[5])(cipherBlock[6])(cipherBlock[7]);

	ThreefishDecrypt1024(cipherBlock,plainBlock);

	Block::Put Buffer1 = Block::Put(xorBlock,outBlock)(plainBlock[0]);
	for(int i=1;i<(BLOCKSIZE/8);++i)
		Buffer1 = Buffer1(plainBlock[i]);
	//Block::Put(xorBlock,outBlock)(plainBlock[0])(plainBlock[1])(plainBlock[2])(plainBlock[3])(plainBlock[4])(plainBlock[5])(plainBlock[6])(plainBlock[7]);
}

NAMESPACE_END
