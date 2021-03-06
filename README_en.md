ASON
====
a json compaticable binary format.
Copyright 2014,Dizsoft Inc. jwang@dizsoft.com

Features
----
1. Json compatible, exchangable with json text format.
2. both in stream mode,structure mode.
3. fast, simple & small.

performance & size
----
data sample      | json size | ason size |ratio%
-----------------|-----------|-----------|-------
webapp.json      |3,554      |2,048      |57.6%
citm_catalog.json|1,727,204  |198,979    |11.5%
citylots.json    |189,778,220|89,104,384 |46.9%

library      |small(3,554)    |medium(1,727,204)   |large(189,778,220)
-------------|----------------|--------------------|---------
ason         |13.0956 ms      |938.235 ms          |3035.1  ms
rapidjson    |15.852 ms       |3416.82 ms          |6604.12 ms
jsoncpp      |58.6507 ms      |12585.8 ms          |33471.5 ms

usage
----
for read from file (both json or ason):
``` cpp
using namespace diz;
std::FILE *file = fopen("test.ason"); // you can open json files too.
AsonValue value = Ason::Parse(f);
fclose(file)
```
read data:
``` cpp
AsonValue obj = value["object"]; // get object
int intval1 = value["it"].getInt(); // "it" must be a int value.
const char* str = value["a"][1].asCString(); // "it" will be casted to int automatically.
```
serialize:
``` cpp
FILE *f = fopen("save.ason"); //  can save as json files too.
Ason::Serialize(value,f); // serialize to ason
Ason::Serialize(value,f,Ason::kFlagOutputJson); // serialize to json
```
ason to json string:
``` cpp
std::string str = Ason::ToJsonString(value);
```

format spec.
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
