# ASON
二进制JSON格式，可与JSON进行相互无缝转换。可用于服务器-客户端通讯的数据传输、作为配置文件格式进行存取等用途。具有高效、
> Copyright 2014 DizSoft Inc. jwang@dizsoft.com

----------------
  
#特性
1. 快速，采用二进制存储处理，执行效率可比字符串格式的JSON快5-10倍（JSONCPP对比测试），存储、传输开销约为JSON的30%-50%
2. 简单，API清晰简洁，易于使用。
3. 方便，与JSON兼容，可互换。再调试期间使用JSON便于查看，发布期间使用ASON减少存储、传输、解析的开销。
4. 支持流模式及结构模式，流模式可用于服务器实时生成数据传输。结构模式更适合于存储配置等已知结构的情况。
5. 支持Headless模式，此模式下无法完全与JSON兼容。但具有更高的解析效率、更少的存储、传输开销。
6. 支持多种语言，c++, java, objective-c(后续将添加）, c#(后续将添加）, lua(后续将添加）

#使用示例
>读取

``` cpp
#include "ason.h"
using namespace diz;

int main(int argc, const char * argv[]) {
    std::FILE *file = fopen("test.ason"); // 可以是JSON或ASON文件.
    AsonValue value = Ason::Parse(f); // 可以从文件或缓冲区等加载
    fclose(file);
}
```

>获取数据

``` cpp
AsonValue obj = value["object"]; // 获取value中名为"object"的子对象
int intval1 = value["it"].getInt(); // 使用get方法必须数据类型一致，在此"it" 必须是一个Int值.
const char* str = value["a"][1].asCString(); // 使用as方法接口会自动转换
```

>序列化、存储

``` cpp
FILE *f = fopen("save.ason");
Ason::Serialize(value,f);
Ason::Serialize(value,f,Ason::kFlagOutputAson); // 保存为ason格式，也可以保存为json
```

>转成Json字符串

``` cpp
std::string str = Ason::ToJsonString(value);
```
>保存为字节流
``` cpp
Ason::BytesBuffer buf = Ason::ToBytes(value);
```

ASON格式规范.
----
>header 4bytes (32bit):
>>`bit31 :` must be 1<br>
>>`bit30 :` big id flag.<br>
>>`bit24-29 :` reserved.<br>
>>`bit0-24 :` dict table size(in bytes).

>Dict Table (*optional)
>>[id][length of name][name string(UTF-8)]......[id][length of name][name string(UTF-8)]<br>
>>`id:` 4bytes when big id flag was set. otherwise 2 bytes.<br>
>>`length of name:` 1byte<br>
>>`name string:` UTF-8 bytes without \0 tail.

>data: [type(1byte)][*value]
>>`type:` (1byte)<br>
>>`value:` (*may not exist)

>>type      |    |value format
----------|----|---------------------------------------------------------------
null      |0x01|not exist
Bool True |0x02|not exist
Bool False|0x03|not exist
int 8bit  |0x10|1byte int
int 16bit |0x11|2byte int (network bytes order)
int 32bit |0x12|4byte int (network bytes order)
int 64bit |0x13|8byte int (network bytes order)
float     |0x1E|4byte float (network bytes order)
double    |0x1F|8byte double (network bytes order)
string    |0xA0|null termiated string.
stirng_1  |0xA1|[len (1byte)][string of len size without \0]
stirng_2  |0xA2|[len (2byte)][string of len size without \0]
stirng_4  |0xA4|[len (4byte)][string of len size without \0]
bytes     |0xD0|[len (1byte)][data byes of len size]...[len (1byte)][data byes of len size][0 (1byte)]
bytes_1   |0xD1|[len (1byte)][data byes of len size]
bytes_2   |0xD2|[len (2byte)][data byes of len size]
bytes_4   |0xD4|[len (4byte)][data byes of len size]
array     |0xE0|[value]....[value][type of 0]
array_1   |0xE1|[len (1byte)][[value]....[value] of len size]
array_2   |0xE2|[len (2byte)][[value]....[value] of len size]
array_4   |0xE4|[len (4byte)][[value]....[value] of len size]
object    |0xF0|[*key][value]....[*key][value][*key of 0]
object_1  |0xF1|[len (1byte)][[*key][value]....[*key][value] of len size]
object_2  |0xF2|[len (2byte)][[*key][value]....[*key][value] of len size]
object_4  |0xF4|[len (4byte)][[*key][value]....[*key][value] of len size]

>>`key :` 4bytes when big id flag was set. otherwise 2 bytes.
