//
// Created by ShakedKod on 3/8/2024.
//

#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum
{
    // value
    OP_CONSTANT,
    // literals
    OP_NIL,
    OP_TRUE,
    OP_FALSE,
    // unary operators
    OP_NEGATE,
    OP_NOT,
    // equality operators
    OP_EQUAL,
    OP_GREATER,
    OP_LESS,
    // binary operators
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    // return
    OP_RETURN
} OpCode;

typedef struct
{
    int count;
    int capacity;
    uint8_t* code;
    int* lines;
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);

void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);

#endif
