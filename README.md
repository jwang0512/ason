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
datasample       | json size | ason size | ratio%
-----------------|-----------|-----------|-------
webapp.ason      |3,554      |2,048      |57.6%
citm_catalog.ason|1,727,204  |198,979    |11.5%

library      |small(3,554)    |medium(1,727,204)   |large
-------------|---------|---------|---------
ason         |0|0|0
rapidjson    |0|0|0
jsoncpp      |0|0|0
msgstack     |0|0|0

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
int 8bit  |0x01|1byte int
int 16bit |0x01|2byte int (network bytes order)
int 32bit |0x01|4byte int (network bytes order)
int 64bit |0x01|8byte int (network bytes order)
float     |0x01|4byte float (network bytes order)
double    |0x01|8byte double (network bytes order)
string    |0x01|null termiated string.
stirng_1  |0x01|[len (1byte)][string of len size without \0]
stirng_2  |0x01|[len (2byte)][string of len size without \0]
stirng_4  |0x01|[len (4byte)][string of len size without \0]
object    |0x01|[*key][value]....[*key][value][*key of 0]
object_1  |0x01|[len (1byte)][[*key][value]....[*key][value] of len size]
object_2  |0x01|[len (2byte)][[*key][value]....[*key][value] of len size]
object_4  |0x01|[len (4byte)][[*key][value]....[*key][value] of len size]
array     |0x01|[value]....[value][type of 0]
array_1   |0x01|[len (1byte)][[value]....[value] of len size]
array_2   |0x01|[len (2byte)][[value]....[value] of len size]
array_4   |0x01|[len (4byte)][[value]....[value] of len size]

>>>`key :` 4bytes when big id flag was set. otherwise 2 bytes.
