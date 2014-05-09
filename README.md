ason
====

abcd
a=int32 (<0 means compressed,abs(a)=original size)
b=int32 (string table size, optional 0=not exist)
c=string table
d=data table

c=efg
e=int32 (key id)
f=int8 (length of g)
g=cstring(without \0)

d=hij
h=int32 (key id)
i=int8 (type)
j=data

Object obj = parse();
Object &obj2 = obj["xxx"];
Object &obj3 = obj[3];

ojb2.toInt();
ojb2.getInt();
obj2.toCString();
obj2.getCString();
obj2.toString();
obj2.getString();
obj2.toDouble();
obj2.getDouble();
obj2.toInt64();
obj2.getInt64();
obj2.toBytes();
obj2.getBytes();
obj2.isNull();
obj2.getType();

obj2.toAson();
obj2.toJson();
