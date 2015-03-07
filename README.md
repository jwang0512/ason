Binary Json Compatible Format.(Exchangable with JSON text format)
====
Features
====
1. Json compatible, exchangable with json text format.
2. both stream mode,structure mode
3. fast
====
usage
====
performance
====
limitions
====
1. strings in UTF-8 only.
====
format spec.
====
[header:4bytes][*dict table: optional][data]

header(32bit):
bit31 : must be 1
bit30 : big id flag.
bit24-29 : reserved.
bit0-24 : dict table size(in bytes).

dict table:
[id][length of name][name string(UTF-8)]......[id][length of name][name string(UTF-8)]
id: 4bytes when big id flag was set. otherwise 2 bytes.
length of name: 1byte
name string: UTF-8 bytes without \0 tail.

data:
[type(1byte)][*value]

type list:
null           not exist
Bool True      not exist
Bool False     not exist
int 8bit       1byte int
int 16bit      2byte int
int 32bit      4byte int
int 64bit      8byte int
float          4byte float
double         8byte double
string         null termiated string.
stirng_1       [len (1byte)][string of len size without \0]
stirng_2       [len (2byte)][string of len size without \0]
stirng_4       [len (4byte)][string of len size without \0]
object         [key][value]....[key][value][key of 0]
object_1       [len (1byte)][[key][value]....[key][value] of len size]
object_2       [len (2byte)][[key][value]....[key][value] of len size]
object_4       [len (4byte)][[key][value]....[key][value] of len size]
array          [value]....[value][type of 0]
array_1        [len (1byte)][[value]....[value] of len size]
array_2        [len (2byte)][[value]....[value] of len size]
array_4        [len (4byte)][[value]....[value] of len size]

key : 4bytes when big id flag was set. otherwise 2 bytes.
