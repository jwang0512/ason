ASON
====

Features
----
1. Json compatible, exchangable with json text format.
2. both stream mode,structure mode
3. fast & small

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
performance
----
library      |small    |medium   |large
-------------|---------|---------|---------
rapidjson    |0|0|0
jsoncpp      |0|0|0
msgstack     |0|0|0

format spec.
----
>header 4bytes (32bit):
>>`bit31 :` must be 1
`bit30 :` big id flag.
`bit24-29 :` reserved.
`bit0-24 :` dict table size(in bytes).

>Dict Table (*optional)
>>[id][length of name][name string(UTF-8)]......[id][length of name][name string(UTF-8)]
`id:` 4bytes when big id flag was set. otherwise 2 bytes.
`length of name:` 1byte
`name string:` UTF-8 bytes without \0 tail.

>data: [type(1byte)][*value]
>>`type:` (1byte)
>>`value:` (*may not exist)

`type list:`
type      |value format
----------|---------
null      |not exist
Bool True |not exist
Bool False|not exist
int 8bit  |1byte int
int 16bit |2byte int (network bytes order)
int 32bit |4byte int (network bytes order)
int 64bit |8byte int (network bytes order)
float     |4byte float (network bytes order)
double    |8byte double (network bytes order)
string    |null termiated string.
stirng_1  |[len (1byte)][string of len size without \0]
stirng_2  |[len (2byte)][string of len size without \0]
stirng_4  |[len (4byte)][string of len size without \0]
object    | [*key][value]....[*key][value][*key of 0]
object_1  |[len (1byte)][[*key][value]....[*key][value] of len size]
object_2  |[len (2byte)][[*key][value]....[*key][value] of len size]
object_4  |[len (4byte)][[*key][value]....[*key][value] of len size]
array     |[value]....[value][type of 0]
array_1   |[len (1byte)][[value]....[value] of len size]
array_2   | [len (2byte)][[value]....[value] of len size]
array_4   |[len (4byte)][[value]....[value] of len size]

`key :` 4bytes when big id flag was set. otherwise 2 bytes.
