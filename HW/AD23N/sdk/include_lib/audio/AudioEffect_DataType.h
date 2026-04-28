#ifndef _AUDIOEFFECT_DATATYPE_
#define _AUDIOEFFECT_DATATYPE_

enum PCMDataType {
    DATA_INT_16BIT = 0,
    DATA_INT_32BIT,
    DATA_FLOAT_32BIT
};

enum {
    af_DATABIT_NOTSUPPORT = 0x404,
};


typedef struct _af_DataType_ {
    unsigned char IndataBit;   //输入数据位宽
    unsigned char OutdataBit;  //输出数据位宽
    char IndataInc;            //输入数据相同通道下一点的步进，单声道步进是1个点，所以选1；双声道步进是2个点，所以选2
    char OutdataInc;           //输出数据相同通道下一点的步进，单声道步进是1个点，所以选1；双声道步进是2个点，所以选2
    char Qval;                 //输入数据的pcm位宽，16bit的pcm位宽是15，24bit的pcm位宽是23
} af_DataType;

#endif // !_AUDIOEFFECT_DATATYPE_
