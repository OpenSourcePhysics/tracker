(function(){var P$=Clazz.newPackage("swingjs.json"),p$1={},I$=[[0,'swingjs.json.JSON','swingjs.JSUtil','java.util.Hashtable',['swingjs.json.JSON','.JSONList','.ListIterator'],['swingjs.json.JSON','.JSONMap'],['swingjs.json.JSON','.JSONReader'],['swingjs.json.JSON','.JSONList']]],I$0=I$[0],$I$=function(i,n,m){return m?$I$(i)[n].apply(null,m):((i=(I$[i]||(I$[i]=Clazz.load(I$0[i])))),!n&&i.$load$&&Clazz.load(i,2),i)};
/*c*/var C$=Clazz.newClass(P$, "JSON", function(){
Clazz.newInstance(this, arguments,0,C$);
});
C$.$classes$=[['JSONReader',9],['JSONMap',10],['JSONList',10]];

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
},1);

C$.$fields$=[[]
,['O',['o','java.lang.Object']]]

Clazz.newMeth(C$, 'setAjax$OA',  function (keyValues) {
var ajax=null;

ajax = {}; if (keyValues[0] == "url" && typeof keyValues[1] == "object") { ajax = keyValues[1].ajax || (keyValues[1].ajax = ajax); } for (var i = 0; i < keyValues.length;) { var key = keyValues[i++];
var val = keyValues[i++]; ajax[key] = val; }
return ajax;
}, 1);

Clazz.newMeth(C$, 'setAjax$java_net_URL',  function (url) {
return C$.setAjax$OA(Clazz.array(java.lang.Object, -1, ["url", url, "dataType", "json", "async", Boolean.FALSE]));
}, 1);

Clazz.newMeth(C$, 'getJSONReader$java_io_InputStream',  function (is) {
return Clazz.new_($I$(6,1).c$$java_io_InputStream,[is]);
}, 1);

Clazz.newMeth(C$, 'parse$O',  function (o) {
if (Clazz.instanceOf(o, "java.lang.String")) return C$.parse$S(o);
if (Clazz.instanceOf(o, "java.io.InputStream")) return C$.parse$java_io_InputStream(o);
if (Clazz.instanceOf(o, "java.io.Reader")) return C$.parse$java_io_Reader(o);
if (Clazz.instanceOf(o, "java.net.URL")) {
return C$.parse$java_net_URL(o);
}return Clazz.new_($I$(6,1).c$$O,[o]).data;
}, 1);

Clazz.newMeth(C$, 'parse$java_io_InputStream',  function (json) {
return Clazz.new_($I$(6,1).c$$java_io_InputStream,[json]).data;
}, 1);

Clazz.newMeth(C$, 'parse$S',  function (json) {
return Clazz.new_($I$(6,1).c$$S,[json]).data;
}, 1);

Clazz.newMeth(C$, 'parse$java_io_Reader',  function (br) {
if (Clazz.instanceOf(br, "swingjs.json.JSON.JSONReader")) return (br).data;
var is=br.$in ||null;
return C$.parse$java_io_InputStream(is);
}, 1);

Clazz.newMeth(C$, 'parse$java_net_URL',  function (url) {
$I$(2).setAjax$java_net_URL(url);
try {
return Clazz.new_([$I$(2,"parseJSON$O",[url.getContent$()])],$I$(6,1).c$$O).data;
} catch (e) {
if (Clazz.exceptionOf(e,"java.io.IOException")){
return null;
} else {
throw e;
}
}
}, 1);

Clazz.newMeth(C$, 'toObject$O',  function (o) {
if (o == null ) return null;
var type=
(typeof o) +
"";
switch (type) {
case "string":
return o;
case "number":
var n=0;
if ((n = o) == (n|0) ||false) return Long.valueOf$J(Clazz.toLong(n));
return Double.valueOf$D(n);
case "boolean":
return Boolean.valueOf$Z(!!o ||false);
case "object":
var isArray=o instanceof Array ||false;
if (isArray) {
return C$.toList$OA(o);
}return (1,o.__CLASS_NAME__ ? o :C$.toMap$O(o));
default:
return o;
}
}, 1);

Clazz.newMeth(C$, 'toMap$O',  function (map) {
return Clazz.new_($I$(5,1).c$$O,[map]);
}, 1);

Clazz.newMeth(C$, 'toList$OA',  function (a) {
return Clazz.new_($I$(7,1).c$$OA,[a]);
}, 1);

C$.$static$=function(){C$.$static$=0;
C$.o=Clazz.getClass($I$(5));
};
;
(function(){/*c*/var C$=Clazz.newClass(P$.JSON, "JSONReader", function(){
Clazz.newInstance(this, arguments[0],false,C$);
}, 'java.io.BufferedReader');

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
},1);

C$.$fields$=[['O',['data','java.lang.Object']]]

Clazz.newMeth(C$, 'c$$java_io_InputStream',  function ($in) {
;C$.superclazz.c$$java_io_Reader.apply(this,[""]);C$.$init$.apply(this);
this.data=$I$(1).toObject$O($in._jsonData || $in.$in && $in.$in._jsonData ||null);
if (this.data == null ) {
var json=($in.str || $in.$in && $in.$in.str ||null);
this.data=$I$(1,"toObject$O",[$I$(2).parseJSONRaw$S(json)]);
}}, 1);

Clazz.newMeth(C$, 'c$$java_io_Reader',  function ($in) {
;C$.superclazz.c$$java_io_Reader.apply(this,[$in]);C$.$init$.apply(this);
this.data=$I$(1).toObject$O($in._jsonData || $in.$in && $in.$in._jsonData ||null);
}, 1);

Clazz.newMeth(C$, 'c$$S',  function (json) {
;C$.superclazz.c$$java_io_Reader.apply(this,[""]);C$.$init$.apply(this);
this.data=$I$(1,"toObject$O",[$I$(2).parseJSONRaw$S(json)]);
}, 1);

Clazz.newMeth(C$, 'c$$O',  function (jsObject) {
;C$.superclazz.c$$java_io_Reader.apply(this,[""]);C$.$init$.apply(this);
this.data=$I$(1).toObject$O(jsObject);
}, 1);

Clazz.newMeth(C$, 'close$',  function () {
this.data=null;
try {
C$.superclazz.prototype.close$.apply(this, []);
} catch (e) {
}
});

Clazz.newMeth(C$);
})()
;
(function(){/*c*/var C$=Clazz.newClass(P$.JSON, "JSONMap", function(){
Clazz.newInstance(this, arguments[0],false,C$);
}, null, 'java.util.Map');

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
},1);

C$.$fields$=[['O',['keys','String[]','map','Object[]','ht','java.util.Map']]]

Clazz.newMeth(C$, 'c$$O',  function (map) {
;C$.$init$.apply(this);
this.map=map;
var keys=Clazz.array(String, [0]);

for (var i in map) keys.push(i);
this.keys=keys;
}, 1);

Clazz.newMeth(C$, 'get$O',  function (key) {
return (this.ht == null  ? $I$(1).toObject$O(this.map[1 ? key :0]) : this.ht.get$O(key));
});

Clazz.newMeth(C$, 'size$',  function () {
return (this.ht == null  ? this.keys.length : this.ht.size$());
});

Clazz.newMeth(C$, 'isEmpty$',  function () {
return (this.ht == null  ? this.keys.length == 0 : this.ht.isEmpty$());
});

Clazz.newMeth(C$, 'containsKey$O',  function (key) {
if (this.ht != null ) return this.ht.containsKey$O(key);
var val=this.get$O(key);
return (1 ? typeof val :val) !== "undefined" ;
});

Clazz.newMeth(C$, 'getHashTable',  function () {
if (this.ht != null ) return this.ht;
var ht=Clazz.new_($I$(3,1));
for (var i=this.keys.length; --i >= 0; ) {
var key=this.keys[i];
ht.put$O$O(key, $I$(1,"toObject$O",[this.get$O(key)]));
}
this.map=null;
this.keys=null;
return this.ht=ht;
}, p$1);

Clazz.newMeth(C$, 'keySet$',  function () {
return p$1.getHashTable.apply(this, []).keySet$();
});

Clazz.newMeth(C$, 'entrySet$',  function () {
return p$1.getHashTable.apply(this, []).entrySet$();
});

Clazz.newMeth(C$, ['put$S$O','put$O$O'],  function (key, value) {
return p$1.getHashTable.apply(this, []).put$O$O(key, value);
});

Clazz.newMeth(C$, 'remove$O',  function (key) {
return p$1.getHashTable.apply(this, []).remove$O(key);
});

Clazz.newMeth(C$, 'putAll$java_util_Map',  function (m) {
p$1.getHashTable.apply(this, []).putAll$java_util_Map(m);
});

Clazz.newMeth(C$, 'clear$',  function () {
p$1.getHashTable.apply(this, []).clear$();
});

Clazz.newMeth(C$, 'containsValue$O',  function (value) {
return p$1.getHashTable.apply(this, []).containsValue$O(value);
});

Clazz.newMeth(C$, 'values$',  function () {
return p$1.getHashTable.apply(this, []).values$();
});

Clazz.newMeth(C$);
})()
;
(function(){/*c*/var C$=Clazz.newClass(P$.JSON, "JSONList", function(){
Clazz.newInstance(this, arguments[0],false,C$);
}, 'java.util.ArrayList');
C$.$classes$=[['ListIterator',9]];

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
},1);

C$.$fields$=[['O',['iter','swingjs.json.JSON.JSONList.ListIterator']]]

Clazz.newMeth(C$, 'c$$OA',  function (a) {
Clazz.super_(C$, this);

this.elementData = a; this.size = a.length;
}, 1);

Clazz.newMeth(C$, 'get$I',  function (i) {
var o=null;

o = this.elementData[i];
return $I$(1).toObject$O(o);
});

Clazz.newMeth(C$, 'iterator$',  function () {
if (this.iter == null ) this.iter=Clazz.new_($I$(4,1));
this.iter.pt=0;
this.iter.list=this;
return this.iter;
});
;
(function(){/*c*/var C$=Clazz.newClass(P$.JSON.JSONList, "ListIterator", function(){
Clazz.newInstance(this, arguments[0],false,C$);
}, null, 'java.util.Iterator');

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
this.pt=-1;
},1);

C$.$fields$=[['I',['pt'],'O',['list','swingjs.json.JSON.JSONList']]]

Clazz.newMeth(C$, 'c$',  function () {
;C$.$init$.apply(this);
}, 1);

Clazz.newMeth(C$, 'hasNext$',  function () {
var more;
{
more = this.list && (this.pt < this.list.size);
if (!more) { this.list = null; this.pt = -1; } return more;
}
});

Clazz.newMeth(C$, 'next$',  function () {
var o=null;

o = this.list.elementData[this.pt++];
return $I$(1).toObject$O(o);
});
})()

Clazz.newMeth(C$);
})()

Clazz.newMeth(C$);
})();
;Clazz.setTVer('5.0.1-v2');//Created 2024-06-20 17:52:26 Java2ScriptVisitor version 5.0.1-v2 net.sf.j2s.core.jar version 5.0.1-v2
