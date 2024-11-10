/*!
  
  // BH 2022.01.12 adds pointer option see BHTEST

 * jQuery JavaScript Library v1.11.0
 * http://jquery.com/
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 *
 * Copyright 2005, 2014 jQuery Foundation, Inc. and other contributors
 * Released under the MIT license
 * http://jquery.org/license
 *
 * Date: 2014-01-23T21:02Z
 */

// modified by Bob Hanson for local MSIE 11 reading remote files and skipping Opera test unless Opera
// 2022.01.08 BHTEST adds pointer| to rmouseEvent

(function( global, factory ) {

	if ( typeof module === "object" && typeof module.exports === "object" ) {
		// For CommonJS and CommonJS-like environments where a proper window is present,
		// execute the factory and get jQuery
		// For environments that do not inherently posses a window with a document
		// (such as Node.js), expose a jQuery-making factory as module.exports
		// This accentuates the need for the creation of a real window
		// e.g. var jQuery = require("jquery")(window);
		// See ticket #14549 for more info
		module.exports = global.document ?
			factory( global, true ) :
			function( w ) {
				if ( !w.document ) {
					throw new Error( "jQuery requires a window with a document" );
				}
				return factory( w );
			};
	} else {
		factory( global );
	}

// Pass this if window is not defined yet
}(typeof window !== "undefined" ? window : this, function( window, noGlobal ) {

// Can't do this because several apps including ASP.NET trace
// the stack via arguments.caller.callee and Firefox dies if
// you try to trace through "use strict" call chains. (#13335)
// Support: Firefox 18+
//

var deletedIds = [];

var slice = deletedIds.slice;

var concat = deletedIds.concat;

var push = deletedIds.push;

var indexOf = deletedIds.indexOf;

var class2type = {};

var toString = class2type.toString;

var hasOwn = class2type.hasOwnProperty;

var trim = "".trim;

var support = {};



var
	version = "1.11.0",

	// Define a local copy of jQuery
	jQuery = function( selector, context ) {
		// The jQuery object is actually just the init constructor 'enhanced'
		// Need init if jQuery is called (just allow error to be thrown if not included)
		return new jQuery.fn.init( selector, context );
	},

	// Make sure we trim BOM and NBSP (here's looking at you, Safari 5.0 and IE)
	rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g,

	// Matches dashed string for camelizing
	rmsPrefix = /^-ms-/,
	rdashAlpha = /-([\da-z])/gi,

	// Used by jQuery.camelCase as callback to replace()
	fcamelCase = function( all, letter ) {
		return letter.toUpperCase();
	};

jQuery.fn = jQuery.prototype = {
	// The current version of jQuery being used
	jquery: version,

	constructor: jQuery,

	// Start with an empty selector
	selector: "",

	// The default length of a jQuery object is 0
	length: 0,

	toArray: function() {
		return slice.call( this );
	},

	// Get the Nth element in the matched element set OR
	// Get the whole matched element set as a clean array
	get: function( num ) {
		return num != null ?

			// Return a 'clean' array
			( num < 0 ? this[ num + this.length ] : this[ num ] ) :

			// Return just the object
			slice.call( this );
	},

	// Take an array of elements and push it onto the stack
	// (returning the new matched element set)
	pushStack: function( elems ) {

		// Build a new jQuery matched element set
		var ret = jQuery.merge( this.constructor(), elems );

		// Add the old object onto the stack (as a reference)
		ret.prevObject = this;
		ret.context = this.context;

		// Return the newly-formed element set
		return ret;
	},

	// Execute a callback for every element in the matched set.
	// (You can seed the arguments with an array of args, but this is
	// only used internally.)
	each: function( callback, args ) {
		return jQuery.each( this, callback, args );
	},

	map: function( callback ) {
		return this.pushStack( jQuery.map(this, function( elem, i ) {
			return callback.call( elem, i, elem );
		}));
	},

	slice: function() {
		return this.pushStack( slice.apply( this, arguments ) );
	},

	first: function() {
		return this.eq( 0 );
	},

	last: function() {
		return this.eq( -1 );
	},

	eq: function( i ) {
		var len = this.length,
			j = +i + ( i < 0 ? len : 0 );
		return this.pushStack( j >= 0 && j < len ? [ this[j] ] : [] );
	},

	end: function() {
		return this.prevObject || this.constructor(null);
	},

	// For internal use only.
	// Behaves like an Array's method, not like a jQuery method.
	push: push,
	sort: deletedIds.sort,
	splice: deletedIds.splice
};

jQuery.extend = jQuery.fn.extend = function() {
	var src, copyIsArray, copy, name, options, clone,
		target = arguments[0] || {},
		i = 1,
		length = arguments.length,
		deep = false;

	// Handle a deep copy situation
	if ( typeof target === "boolean" ) {
		deep = target;

		// skip the boolean and the target
		target = arguments[ i ] || {};
		i++;
	}

	// Handle case when target is a string or something (possible in deep copy)
	if ( typeof target !== "object" && !jQuery.isFunction(target) ) {
		target = {};
	}

	// extend jQuery itself if only one argument is passed
	if ( i === length ) {
		target = this;
		i--;
	}

	for ( ; i < length; i++ ) {
		// Only deal with non-null/undefined values
		if ( (options = arguments[ i ]) != null ) {
			// Extend the base object
			for ( name in options ) {
				src = target[ name ];
				copy = options[ name ];

				// Prevent never-ending loop
				if ( target === copy ) {
					continue;
				}

				// Recurse if we're merging plain objects or arrays
				if ( deep && copy && ( jQuery.isPlainObject(copy) || (copyIsArray = jQuery.isArray(copy)) ) ) {
					if ( copyIsArray ) {
						copyIsArray = false;
						clone = src && jQuery.isArray(src) ? src : [];

					} else {
						clone = src && jQuery.isPlainObject(src) ? src : {};
					}

					// Never move original objects, clone them
					target[ name ] = jQuery.extend( deep, clone, copy );

				// Don't bring in undefined values
				} else if ( copy !== undefined ) {
					target[ name ] = copy;
				}
			}
		}
	}

	// Return the modified object
	return target;
};

jQuery.extend({
	// Unique for each copy of jQuery on the page
	expando: "jQuery" + ( version + Math.random() ).replace( /\D/g, "" ),

	// Assume jQuery is ready without the ready module
	isReady: true,

	error: function( msg ) {
		throw new Error( msg );
	},

	noop: function() {},

	// See test/unit/core.js for details concerning isFunction.
	// Since version 1.3, DOM methods and functions like alert
	// aren't supported. They return false on IE (#2968).
	isFunction: function( obj ) {
		return jQuery.type(obj) === "function";
	},

	isArray: Array.isArray || function( obj ) {
		return jQuery.type(obj) === "array";
	},

	isWindow: function( obj ) {
		/* jshint eqeqeq: false */
		return obj != null && obj == obj.window;
	},

	isNumeric: function( obj ) {
		// parseFloat NaNs numeric-cast false positives (null|true|false|"")
		// ...but misinterprets leading-number strings, particularly hex literals ("0x...")
		// subtraction forces infinities to NaN
		return obj - parseFloat( obj ) >= 0;
	},

	isEmptyObject: function( obj ) {
		var name;
		for ( name in obj ) {
			return false;
		}
		return true;
	},

	isPlainObject: function( obj ) {
		var key;

		// Must be an Object.
		// Because of IE, we also have to check the presence of the constructor property.
		// Make sure that DOM nodes and window objects don't pass through, as well
		if ( !obj || jQuery.type(obj) !== "object" || obj.nodeType || jQuery.isWindow( obj ) ) {
			return false;
		}

		try {
			// Not own constructor property must be Object
			if ( obj.constructor &&
				!hasOwn.call(obj, "constructor") &&
				!hasOwn.call(obj.constructor.prototype, "isPrototypeOf") ) {
				return false;
			}
		} catch ( e ) {
			// IE8,9 Will throw exceptions on certain host objects #9897
			return false;
		}

		// Support: IE<9
		// Handle iteration over inherited properties before own properties.
		if ( support.ownLast ) {
			for ( key in obj ) {
				return hasOwn.call( obj, key );
			}
		}

		// Own properties are enumerated firstly, so to speed up,
		// if last one is own, then all properties are own.
		for ( key in obj ) {}

		return key === undefined || hasOwn.call( obj, key );
	},

	type: function( obj ) {
		if ( obj == null ) {
			return obj + "";
		}
		return typeof obj === "object" || typeof obj === "function" ?
			class2type[ toString.call(obj) ] || "object" :
			typeof obj;
	},

	// Evaluates a script in a global context
	// Workarounds based on findings by Jim Driscoll
	// http://weblogs.java.net/blog/driscoll/archive/2009/09/08/eval-javascript-global-context
	globalEval: function( data ) {
		if ( data && jQuery.trim( data ) ) {
			// We use execScript on Internet Explorer
			// We use an anonymous function so that context is window
			// rather than jQuery in Firefox
			( window.execScript || function( data ) {
				window[ "eval" ].call( window, data );
			} )( data );
		}
	},

	// Convert dashed to camelCase; used by the css and data modules
	// Microsoft forgot to hump their vendor prefix (#9572)
	camelCase: function( string ) {
		return string.replace( rmsPrefix, "ms-" ).replace( rdashAlpha, fcamelCase );
	},

	nodeName: function( elem, name ) {
		return elem.nodeName && elem.nodeName.toLowerCase() === name.toLowerCase();
	},

	// args is for internal usage only
	each: function( obj, callback, args ) {
		var value,
			i = 0,
			length = obj.length,
			isArray = isArraylike( obj );

		if ( args ) {
			if ( isArray ) {
				for ( ; i < length; i++ ) {
					value = callback.apply( obj[ i ], args );

					if ( value === false ) {
						break;
					}
				}
			} else {
				for ( i in obj ) {
					value = callback.apply( obj[ i ], args );

					if ( value === false ) {
						break;
					}
				}
			}

		// A special, fast, case for the most common use of each
		} else {
			if ( isArray ) {
				for ( ; i < length; i++ ) {
					value = callback.call( obj[ i ], i, obj[ i ] );

					if ( value === false ) {
						break;
					}
				}
			} else {
				for ( i in obj ) {
					value = callback.call( obj[ i ], i, obj[ i ] );

					if ( value === false ) {
						break;
					}
				}
			}
		}

		return obj;
	},

	// Use native String.trim function wherever possible
	trim: trim && !trim.call("\uFEFF\xA0") ?
		function( text ) {
			return text == null ?
				"" :
				trim.call( text );
		} :

		// Otherwise use our own trimming functionality
		function( text ) {
			return text == null ?
				"" :
				( text + "" ).replace( rtrim, "" );
		},

	// results is for internal usage only
	makeArray: function( arr, results ) {
		var ret = results || [];

		if ( arr != null ) {
			if ( isArraylike( Object(arr) ) ) {
				jQuery.merge( ret,
					typeof arr === "string" ?
					[ arr ] : arr
				);
			} else {
				push.call( ret, arr );
			}
		}

		return ret;
	},

	inArray: function( elem, arr, i ) {
		var len;

		if ( arr ) {
			if ( indexOf ) {
				return indexOf.call( arr, elem, i );
			}

			len = arr.length;
			i = i ? i < 0 ? Math.max( 0, len + i ) : i : 0;

			for ( ; i < len; i++ ) {
				// Skip accessing in sparse arrays
				if ( i in arr && arr[ i ] === elem ) {
					return i;
				}
			}
		}

		return -1;
	},

	merge: function( first, second ) {
		var len = +second.length,
			j = 0,
			i = first.length;

		while ( j < len ) {
			first[ i++ ] = second[ j++ ];
		}

		// Support: IE<9
		// Workaround casting of .length to NaN on otherwise arraylike objects (e.g., NodeLists)
		if ( len !== len ) {
			while ( second[j] !== undefined ) {
				first[ i++ ] = second[ j++ ];
			}
		}

		first.length = i;

		return first;
	},

	grep: function( elems, callback, invert ) {
		var callbackInverse,
			matches = [],
			i = 0,
			length = elems.length,
			callbackExpect = !invert;

		// Go through the array, only saving the items
		// that pass the validator function
		for ( ; i < length; i++ ) {
			callbackInverse = !callback( elems[ i ], i );
			if ( callbackInverse !== callbackExpect ) {
				matches.push( elems[ i ] );
			}
		}

		return matches;
	},

	// arg is for internal usage only
	map: function( elems, callback, arg ) {
		var value,
			i = 0,
			length = elems.length,
			isArray = isArraylike( elems ),
			ret = [];

		// Go through the array, translating each of the items to their new values
		if ( isArray ) {
			for ( ; i < length; i++ ) {
				value = callback( elems[ i ], i, arg );

				if ( value != null ) {
					ret.push( value );
				}
			}

		// Go through every key on the object,
		} else {
			for ( i in elems ) {
				value = callback( elems[ i ], i, arg );

				if ( value != null ) {
					ret.push( value );
				}
			}
		}

		// Flatten any nested arrays
		return concat.apply( [], ret );
	},

	// A global GUID counter for objects
	guid: 1,

	// Bind a function to a context, optionally partially applying any
	// arguments.
	proxy: function( fn, context ) {
		var args, proxy, tmp;

		if ( typeof context === "string" ) {
			tmp = fn[ context ];
			context = fn;
			fn = tmp;
		}

		// Quick check to determine if target is callable, in the spec
		// this throws a TypeError, but we will just return undefined.
		if ( !jQuery.isFunction( fn ) ) {
			return undefined;
		}

		// Simulated bind
		args = slice.call( arguments, 2 );
		proxy = function() {
			return fn.apply( context || this, args.concat( slice.call( arguments ) ) );
		};

		// Set the guid of unique handler to the same of original handler, so it can be removed
		proxy.guid = fn.guid = fn.guid || jQuery.guid++;

		return proxy;
	},

	now: function() {
		return +( new Date() );
	},

	// jQuery.support is not used in Core but other projects attach their
	// properties to it so it needs to exist.
	support: support
});

// Populate the class2type map
jQuery.each("Boolean Number String Function Array Date RegExp Object Error".split(" "), function(i, name) {
	class2type[ "[object " + name + "]" ] = name.toLowerCase();
});

function isArraylike( obj ) {
	var length = obj.length,
		type = jQuery.type( obj );

	if ( type === "function" || jQuery.isWindow( obj ) ) {
		return false;
	}

	if ( obj.nodeType === 1 && length ) {
		return true;
	}

	return type === "array" || length === 0 ||
		typeof length === "number" && length > 0 && ( length - 1 ) in obj;
}
var Sizzle =
/*!
 * Sizzle CSS Selector Engine v1.10.16
 * http://sizzlejs.com/
 *
 * Copyright 2013 jQuery Foundation, Inc. and other contributors
 * Released under the MIT license
 * http://jquery.org/license
 *
 * Date: 2014-01-13
 */
(function( window ) {

var i,
	support,
	Expr,
	getText,
	isXML,
	compile,
	outermostContext,
	sortInput,
	hasDuplicate,

	// Local document vars
	setDocument,
	document,
	docElem,
	documentIsHTML,
	rbuggyQSA,
	rbuggyMatches,
	matches,
	contains,

	// Instance-specific data
	expando = "sizzle" + -(new Date()),
	preferredDoc = window.document,
	dirruns = 0,
	done = 0,
	classCache = createCache(),
	tokenCache = createCache(),
	compilerCache = createCache(),
	sortOrder = function( a, b ) {
		if ( a === b ) {
			hasDuplicate = true;
		}
		return 0;
	},

	// General-purpose constants
	strundefined = typeof undefined,
	MAX_NEGATIVE = 1 << 31,

	// Instance methods
	hasOwn = ({}).hasOwnProperty,
	arr = [],
	pop = arr.pop,
	push_native = arr.push,
	push = arr.push,
	slice = arr.slice,
	// Use a stripped-down indexOf if we can't use a native one
	indexOf = arr.indexOf || function( elem ) {
		var i = 0,
			len = this.length;
		for ( ; i < len; i++ ) {
			if ( this[i] === elem ) {
				return i;
			}
		}
		return -1;
	},

	booleans = "checked|selected|async|autofocus|autoplay|controls|defer|disabled|hidden|ismap|loop|multiple|open|readonly|required|scoped",

	// Regular expressions

	// Whitespace characters http://www.w3.org/TR/css3-selectors/#whitespace
	whitespace = "[\\x20\\t\\r\\n\\f]",
	// http://www.w3.org/TR/css3-syntax/#characters
	characterEncoding = "(?:\\\\.|[\\w-]|[^\\x00-\\xa0])+",

	// Loosely modeled on CSS identifier characters
	// An unquoted value should be a CSS identifier http://www.w3.org/TR/css3-selectors/#attribute-selectors
	// Proper syntax: http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier
	identifier = characterEncoding.replace( "w", "w#" ),

	// Acceptable operators http://www.w3.org/TR/selectors/#attribute-selectors
	attributes = "\\[" + whitespace + "*(" + characterEncoding + ")" + whitespace +
		"*(?:([*^$|!~]?=)" + whitespace + "*(?:(['\"])((?:\\\\.|[^\\\\])*?)\\3|(" + identifier + ")|)|)" + whitespace + "*\\]",

	// Prefer arguments quoted,
	//   then not containing pseudos/brackets,
	//   then attribute selectors/non-parenthetical expressions,
	//   then anything else
	// These preferences are here to reduce the number of selectors
	//   needing tokenize in the PSEUDO preFilter
	pseudos = ":(" + characterEncoding + ")(?:\\(((['\"])((?:\\\\.|[^\\\\])*?)\\3|((?:\\\\.|[^\\\\()[\\]]|" + attributes.replace( 3, 8 ) + ")*)|.*)\\)|)",

	// Leading and non-escaped trailing whitespace, capturing some non-whitespace characters preceding the latter
	rtrim = new RegExp( "^" + whitespace + "+|((?:^|[^\\\\])(?:\\\\.)*)" + whitespace + "+$", "g" ),

	rcomma = new RegExp( "^" + whitespace + "*," + whitespace + "*" ),
	rcombinators = new RegExp( "^" + whitespace + "*([>+~]|" + whitespace + ")" + whitespace + "*" ),

	rattributeQuotes = new RegExp( "=" + whitespace + "*([^\\]'\"]*?)" + whitespace + "*\\]", "g" ),

	rpseudo = new RegExp( pseudos ),
	ridentifier = new RegExp( "^" + identifier + "$" ),

	matchExpr = {
		"ID": new RegExp( "^#(" + characterEncoding + ")" ),
		"CLASS": new RegExp( "^\\.(" + characterEncoding + ")" ),
		"TAG": new RegExp( "^(" + characterEncoding.replace( "w", "w*" ) + ")" ),
		"ATTR": new RegExp( "^" + attributes ),
		"PSEUDO": new RegExp( "^" + pseudos ),
		"CHILD": new RegExp( "^:(only|first|last|nth|nth-last)-(child|of-type)(?:\\(" + whitespace +
			"*(even|odd|(([+-]|)(\\d*)n|)" + whitespace + "*(?:([+-]|)" + whitespace +
			"*(\\d+)|))" + whitespace + "*\\)|)", "i" ),
		"bool": new RegExp( "^(?:" + booleans + ")$", "i" ),
		// For use in libraries implementing .is()
		// We use this for POS matching in `select`
		"needsContext": new RegExp( "^" + whitespace + "*[>+~]|:(even|odd|eq|gt|lt|nth|first|last)(?:\\(" +
			whitespace + "*((?:-\\d)?\\d*)" + whitespace + "*\\)|)(?=[^-]|$)", "i" )
	},

	rinputs = /^(?:input|select|textarea|button)$/i,
	rheader = /^h\d$/i,

	rnative = /^[^{]+\{\s*\[native \w/,

	// Easily-parseable/retrievable ID or TAG or CLASS selectors
	rquickExpr = /^(?:#([\w-]+)|(\w+)|\.([\w-]+))$/,

	rsibling = /[+~]/,
	rescape = /'|\\/g,

	// CSS escapes http://www.w3.org/TR/CSS21/syndata.html#escaped-characters
	runescape = new RegExp( "\\\\([\\da-f]{1,6}" + whitespace + "?|(" + whitespace + ")|.)", "ig" ),
	funescape = function( _, escaped, escapedWhitespace ) {
		var high = "0x" + escaped - 0x10000;
		// NaN means non-codepoint
		// Support: Firefox
		// Workaround erroneous numeric interpretation of +"0x"
		return high !== high || escapedWhitespace ?
			escaped :
			high < 0 ?
				// BMP codepoint
				String.fromCharCode( high + 0x10000 ) :
				// Supplemental Plane codepoint (surrogate pair)
				String.fromCharCode( high >> 10 | 0xD800, high & 0x3FF | 0xDC00 );
	};

// Optimize for push.apply( _, NodeList )
try {
	push.apply(
		(arr = slice.call( preferredDoc.childNodes )),
		preferredDoc.childNodes
	);
	// Support: Android<4.0
	// Detect silently failing push.apply
	arr[ preferredDoc.childNodes.length ].nodeType;
} catch ( e ) {
	push = { apply: arr.length ?

		// Leverage slice if possible
		function( target, els ) {
			push_native.apply( target, slice.call(els) );
		} :

		// Support: IE<9
		// Otherwise append directly
		function( target, els ) {
			var j = target.length,
				i = 0;
			// Can't trust NodeList.length
			while ( (target[j++] = els[i++]) ) {}
			target.length = j - 1;
		}
	};
}

var j2sInvalidSelectors = "";

function Sizzle( selector, context, results, seed ) {
	var match, elem, m, nodeType,
		// QSA vars
		i, groups, old, nid, newContext, newSelector;

	if ( ( context ? context.ownerDocument || context : preferredDoc ) !== document ) {
		setDocument( context );
	}

	context = context || document;
	results = results || [];

	if ( !selector || typeof selector !== "string" ) {
		return results;
	}

	if ( (nodeType = context.nodeType) !== 1 && nodeType !== 9 ) {
		return [];
	}

	if ( documentIsHTML && !seed ) {

		// Shortcuts
		if ( (match = rquickExpr.exec( selector )) ) {
			// Speed-up: Sizzle("#ID")
			if ( (m = match[1]) ) {
				if ( nodeType === 9 ) {
					elem = context.getElementById( m );
					// Check parentNode to catch when Blackberry 4.6 returns
					// nodes that are no longer in the document (jQuery #6963)
					if ( elem && elem.parentNode ) {
						// Handle the case where IE, Opera, and Webkit return items
						// by name instead of ID
						if ( elem.id === m ) {
							results.push( elem );
							return results;
						}
					} else {
						return results;
					}
				} else {
					// Context is not a document
					if ( context.ownerDocument && (elem = context.ownerDocument.getElementById( m )) &&
						contains( context, elem ) && elem.id === m ) {
						results.push( elem );
						return results;
					}
				}

			// Speed-up: Sizzle("TAG")
			} else if ( match[2] ) {
				push.apply( results, context.getElementsByTagName( selector ) );
				return results;

			// Speed-up: Sizzle(".CLASS")
			} else if ( (m = match[3]) && support.getElementsByClassName && context.getElementsByClassName ) {
				push.apply( results, context.getElementsByClassName( m ) );
				return results;
			}
		}

		// QSA path
		if ( support.qsa && (!rbuggyQSA || !rbuggyQSA.test( selector )) ) {
			nid = old = expando;
			newContext = context;
			newSelector = nodeType === 9 && selector;

			// qSA works strangely on Element-rooted queries
			// We can work around this by specifying an extra ID on the root
			// and working up from there (Thanks to Andrew Dupont for the technique)
			// IE 8 doesn't work on object elements
			if ( nodeType === 1 && context.nodeName.toLowerCase() !== "object" ) {
				groups = tokenize( selector );

				if ( (old = context.getAttribute("id")) ) {
					nid = old.replace( rescape, "\\$&" );
				} else {
					context.setAttribute( "id", nid );
				}
				nid = "[id='" + nid + "'] ";

				i = groups.length;
				while ( i-- ) {
					groups[i] = nid + toSelector( groups[i] );
				}
				newContext = rsibling.test( selector ) && testContext( context.parentNode ) || context;
				newSelector = groups.join(",");
			}

			if ( newSelector ) {
				try {
					 // SwingJS addition
					var mode = (newSelector.indexOf(":has(") >= 0 ? ":has(" 
							: newSelector.indexOf("!=") >= 0 ? "!="
							: newSelector.indexOf(":") >= 0 ? ":" 
							: 0
					);
					 if (mode && j2sInvalidSelectors.indexOf(mode) < 0) {			
						push.apply( results,
							newContext.querySelectorAll( newSelector )
						);
						return results;
					 }
				} catch(qsaError) {
					if (mode)
						j2sInvalidSelectors += mode;
				} finally {
					if ( !old ) {
						context.removeAttribute("id");
					}
				}
			}
		}
	}

	// All others
	return select( selector.replace( rtrim, "$1" ), context, results, seed );
}

/**
 * Create key-value caches of limited size
 * @returns {Function(string, Object)} Returns the Object data after storing it on itself with
 *	property name the (space-suffixed) string and (if the cache is larger than Expr.cacheLength)
 *	deleting the oldest entry
 */
function createCache() {
	var keys = [];

	function cache( key, value ) {
		// Use (key + " ") to avoid collision with native prototype properties (see Issue #157)
		if ( keys.push( key + " " ) > Expr.cacheLength ) {
			// Only keep the most recent entries
			delete cache[ keys.shift() ];
		}
		return (cache[ key + " " ] = value);
	}
	return cache;
}

/**
 * Mark a function for special use by Sizzle
 * @param {Function} fn The function to mark
 */
function markFunction( fn ) {
	fn[ expando ] = true;
	return fn;
}

/**
 * Support testing using an element
 * @param {Function} fn Passed the created div and expects a boolean result
 */
function assert( fn ) {
	var div = document.createElement("div");

	try {
		return !!fn( div );
	} catch (e) {
		return false;
	} finally {
		// Remove from its parent by default
		if ( div.parentNode ) {
			div.parentNode.removeChild( div );
		}
		// release memory in IE
		div = null;
	}
}

/**
 * Adds the same handler for all of the specified attrs
 * @param {String} attrs Pipe-separated list of attributes
 * @param {Function} handler The method that will be applied
 */
function addHandle( attrs, handler ) {
	var arr = attrs.split("|"),
		i = attrs.length;

	while ( i-- ) {
		Expr.attrHandle[ arr[i] ] = handler;
	}
}

/**
 * Checks document order of two siblings
 * @param {Element} a
 * @param {Element} b
 * @returns {Number} Returns less than 0 if a precedes b, greater than 0 if a follows b
 */
function siblingCheck( a, b ) {
	var cur = b && a,
		diff = cur && a.nodeType === 1 && b.nodeType === 1 &&
			( ~b.sourceIndex || MAX_NEGATIVE ) -
			( ~a.sourceIndex || MAX_NEGATIVE );

	// Use IE sourceIndex if available on both nodes
	if ( diff ) {
		return diff;
	}

	// Check if b follows a
	if ( cur ) {
		while ( (cur = cur.nextSibling) ) {
			if ( cur === b ) {
				return -1;
			}
		}
	}

	return a ? 1 : -1;
}

/**
 * Returns a function to use in pseudos for input types
 * @param {String} type
 */
function createInputPseudo( type ) {
	return function( elem ) {
		var name = elem.nodeName.toLowerCase();
		return name === "input" && elem.type === type;
	};
}

/**
 * Returns a function to use in pseudos for buttons
 * @param {String} type
 */
function createButtonPseudo( type ) {
	return function( elem ) {
		var name = elem.nodeName.toLowerCase();
		return (name === "input" || name === "button") && elem.type === type;
	};
}

/**
 * Returns a function to use in pseudos for positionals
 * @param {Function} fn
 */
function createPositionalPseudo( fn ) {
	return markFunction(function( argument ) {
		argument = +argument;
		return markFunction(function( seed, matches ) {
			var j,
				matchIndexes = fn( [], seed.length, argument ),
				i = matchIndexes.length;

			// Match elements found at the specified indexes
			while ( i-- ) {
				if ( seed[ (j = matchIndexes[i]) ] ) {
					seed[j] = !(matches[j] = seed[j]);
				}
			}
		});
	});
}

/**
 * Checks a node for validity as a Sizzle context
 * @param {Element|Object=} context
 * @returns {Element|Object|Boolean} The input node if acceptable, otherwise a falsy value
 */
function testContext( context ) {
	return context && typeof context.getElementsByTagName !== strundefined && context;
}

// Expose support vars for convenience
support = Sizzle.support = {};

/**
 * Detects XML nodes
 * @param {Element|Object} elem An element or a document
 * @returns {Boolean} True iff elem is a non-HTML XML node
 */
isXML = Sizzle.isXML = function( elem ) {
	// documentElement is verified for cases where it doesn't yet exist
	// (such as loading iframes in IE - #4833)
	var documentElement = elem && (elem.ownerDocument || elem).documentElement;
	return documentElement ? documentElement.nodeName !== "HTML" : false;
};

/**
 * Sets document-related variables once based on the current document
 * @param {Element|Object} [doc] An element or document object to use to set the document
 * @returns {Object} Returns the current document
 */
setDocument = Sizzle.setDocument = function( node ) {
	var hasCompare,
		doc = node ? node.ownerDocument || node : preferredDoc,
		parent = doc.defaultView;

	// If no document and documentElement is available, return
	if ( doc === document || doc.nodeType !== 9 || !doc.documentElement ) {
		return document;
	}

	// Set our document
	document = doc;
	docElem = doc.documentElement;

	// Support tests
	documentIsHTML = !isXML( doc );

	// Support: IE>8
	// If iframe document is assigned to "document" variable and if iframe has been reloaded,
	// IE will throw "permission denied" error when accessing "document" variable, see jQuery #13936
	// IE6-8 do not support the defaultView property so parent will be undefined
	if ( parent && parent !== parent.top ) {
		// IE11 does not have attachEvent, so all must suffer
		if ( parent.addEventListener ) {
			parent.addEventListener( "unload", function() {
				setDocument();
			}, false );
		} else if ( parent.attachEvent ) {
			parent.attachEvent( "onunload", function() {
				setDocument();
			});
		}
	}

	/* Attributes
	---------------------------------------------------------------------- */

	// Support: IE<8
	// Verify that getAttribute really returns attributes and not properties (excepting IE8 booleans)
	support.attributes = assert(function( div ) {
		div.className = "i";
		return !div.getAttribute("className");
	});

	/* getElement(s)By*
	---------------------------------------------------------------------- */

	// Check if getElementsByTagName("*") returns only elements
	support.getElementsByTagName = assert(function( div ) {
		div.appendChild( doc.createComment("") );
		return !div.getElementsByTagName("*").length;
	});

	// Check if getElementsByClassName can be trusted
	support.getElementsByClassName = rnative.test( doc.getElementsByClassName ) && assert(function( div ) {
		div.innerHTML = "<div class='a'></div><div class='a i'></div>";

		// Support: Safari<4
		// Catch class over-caching
		div.firstChild.className = "i";
		// Support: Opera<10
		// Catch gEBCN failure to find non-leading classes
		return div.getElementsByClassName("i").length === 2;
	});

	// Support: IE<10
	// Check if getElementById returns elements by name
	// The broken getElementById methods don't pick up programatically-set names,
	// so use a roundabout getElementsByName test
	support.getById = assert(function( div ) {
		docElem.appendChild( div ).id = expando;
		return !doc.getElementsByName || !doc.getElementsByName( expando ).length;
	});

	// ID find and filter
	if ( support.getById ) {
		Expr.find["ID"] = function( id, context ) {
			if ( typeof context.getElementById !== strundefined && documentIsHTML ) {
				var m = context.getElementById( id );
				// Check parentNode to catch when Blackberry 4.6 returns
				// nodes that are no longer in the document #6963
				return m && m.parentNode ? [m] : [];
			}
		};
		Expr.filter["ID"] = function( id ) {
			var attrId = id.replace( runescape, funescape );
			return function( elem ) {
				return elem.getAttribute("id") === attrId;
			};
		};
	} else {
		// Support: IE6/7
		// getElementById is not reliable as a find shortcut
		delete Expr.find["ID"];

		Expr.filter["ID"] =  function( id ) {
			var attrId = id.replace( runescape, funescape );
			return function( elem ) {
				var node = typeof elem.getAttributeNode !== strundefined && elem.getAttributeNode("id");
				return node && node.value === attrId;
			};
		};
	}

	// Tag
	Expr.find["TAG"] = support.getElementsByTagName ?
		function( tag, context ) {
			if ( typeof context.getElementsByTagName !== strundefined ) {
				return context.getElementsByTagName( tag );
			}
		} :
		function( tag, context ) {
			var elem,
				tmp = [],
				i = 0,
				results = context.getElementsByTagName( tag );

			// Filter out possible comments
			if ( tag === "*" ) {
				while ( (elem = results[i++]) ) {
					if ( elem.nodeType === 1 ) {
						tmp.push( elem );
					}
				}

				return tmp;
			}
			return results;
		};

	// Class
	Expr.find["CLASS"] = support.getElementsByClassName && function( className, context ) {
		if ( typeof context.getElementsByClassName !== strundefined && documentIsHTML ) {
			return context.getElementsByClassName( className );
		}
	};

	/* QSA/matchesSelector
	---------------------------------------------------------------------- */

	// QSA and matchesSelector support

	// matchesSelector(:active) reports false when true (IE9/Opera 11.5)
	rbuggyMatches = [];

	// qSa(:focus) reports false when true (Chrome 21)
	// We allow this because of a bug in IE8/9 that throws an error
	// whenever `document.activeElement` is accessed on an iframe
	// So, we allow :focus to pass through QSA all the time to avoid the IE error
	// See http://bugs.jquery.com/ticket/13378
	rbuggyQSA = [];

	if ( (support.qsa = rnative.test( doc.querySelectorAll )) ) {
		// Build QSA regex
		// Regex strategy adopted from Diego Perini
		assert(function( div ) {
			// Select is set to empty string on purpose
			// This is to test IE's treatment of not explicitly
			// setting a boolean content attribute,
			// since its presence should be enough
			// http://bugs.jquery.com/ticket/12359
			div.innerHTML = "<select t=''><option selected=''></option></select>";

			// Support: IE8, Opera 10-12
			// Nothing should be selected when empty strings follow ^= or $= or *=
			if ( div.querySelectorAll("[t^='']").length ) {
				rbuggyQSA.push( "[*^$]=" + whitespace + "*(?:''|\"\")" );
			}

			// Support: IE8
			// Boolean attributes and "value" are not treated correctly
			if ( !div.querySelectorAll("[selected]").length ) {
				rbuggyQSA.push( "\\[" + whitespace + "*(?:value|" + booleans + ")" );
			}

			// Webkit/Opera - :checked should return selected option elements
			// http://www.w3.org/TR/2011/REC-css3-selectors-20110929/#checked
			// IE8 throws error here and will not see later tests
			if ( !div.querySelectorAll(":checked").length ) {
				rbuggyQSA.push(":checked");
			}
		});

		assert(function( div ) {
			// Support: Windows 8 Native Apps
			// The type and name attributes are restricted during .innerHTML assignment
			var input = doc.createElement("input");
			input.setAttribute( "type", "hidden" );
			div.appendChild( input ).setAttribute( "name", "D" );

			// Support: IE8
			// Enforce case-sensitivity of name attribute
			if ( div.querySelectorAll("[name=d]").length ) {
				rbuggyQSA.push( "name" + whitespace + "*[*^$|!~]?=" );
			}

			// FF 3.5 - :enabled/:disabled and hidden elements (hidden elements are still enabled)
			// IE8 throws error here and will not see later tests
			if ( !div.querySelectorAll(":enabled").length ) {
				rbuggyQSA.push( ":enabled", ":disabled" );
			}

			// Opera 10-11 does not throw on post-comma invalid pseudos
			// BH 2018
			if (navigator.userAgent && navigator.userAgent.indexOf("Opera") >= 0) {
				div.querySelectorAll("*,:x");
				rbuggyQSA.push(",.*:");
			}
		});
	}

	if ( (support.matchesSelector = rnative.test( (matches = docElem.webkitMatchesSelector ||
		docElem.mozMatchesSelector ||
		docElem.oMatchesSelector ||
		docElem.msMatchesSelector) )) ) {

		assert(function( div ) {
			// Check to see if it's possible to do matchesSelector
			// on a disconnected node (IE 9)
			support.disconnectedMatch = matches.call( div, "div" );

			// This should fail with an exception
			// Gecko does not error, returns false instead
			matches.call( div, "[s!='']:x" );
			rbuggyMatches.push( "!=", pseudos );
		});
	}

	rbuggyQSA = rbuggyQSA.length && new RegExp( rbuggyQSA.join("|") );
	rbuggyMatches = rbuggyMatches.length && new RegExp( rbuggyMatches.join("|") );

	/* Contains
	---------------------------------------------------------------------- */
	hasCompare = rnative.test( docElem.compareDocumentPosition );

	// Element contains another
	// Purposefully does not implement inclusive descendent
	// As in, an element does not contain itself
	contains = hasCompare || rnative.test( docElem.contains ) ?
		function( a, b ) {
			var adown = a.nodeType === 9 ? a.documentElement : a,
				bup = b && b.parentNode;
			return a === bup || !!( bup && bup.nodeType === 1 && (
				adown.contains ?
					adown.contains( bup ) :
					a.compareDocumentPosition && a.compareDocumentPosition( bup ) & 16
			));
		} :
		function( a, b ) {
			if ( b ) {
				while ( (b = b.parentNode) ) {
					if ( b === a ) {
						return true;
					}
				}
			}
			return false;
		};

	/* Sorting
	---------------------------------------------------------------------- */

	// Document order sorting
	sortOrder = hasCompare ?
	function( a, b ) {

		// Flag for duplicate removal
		if ( a === b ) {
			hasDuplicate = true;
			return 0;
		}

		// Sort on method existence if only one input has compareDocumentPosition
		var compare = !a.compareDocumentPosition - !b.compareDocumentPosition;
		if ( compare ) {
			return compare;
		}

		// Calculate position if both inputs belong to the same document
		compare = ( a.ownerDocument || a ) === ( b.ownerDocument || b ) ?
			a.compareDocumentPosition( b ) :

			// Otherwise we know they are disconnected
			1;

		// Disconnected nodes
		if ( compare & 1 ||
			(!support.sortDetached && b.compareDocumentPosition( a ) === compare) ) {

			// Choose the first element that is related to our preferred document
			if ( a === doc || a.ownerDocument === preferredDoc && contains(preferredDoc, a) ) {
				return -1;
			}
			if ( b === doc || b.ownerDocument === preferredDoc && contains(preferredDoc, b) ) {
				return 1;
			}

			// Maintain original order
			return sortInput ?
				( indexOf.call( sortInput, a ) - indexOf.call( sortInput, b ) ) :
				0;
		}

		return compare & 4 ? -1 : 1;
	} :
	function( a, b ) {
		// Exit early if the nodes are identical
		if ( a === b ) {
			hasDuplicate = true;
			return 0;
		}

		var cur,
			i = 0,
			aup = a.parentNode,
			bup = b.parentNode,
			ap = [ a ],
			bp = [ b ];

		// Parentless nodes are either documents or disconnected
		if ( !aup || !bup ) {
			return a === doc ? -1 :
				b === doc ? 1 :
				aup ? -1 :
				bup ? 1 :
				sortInput ?
				( indexOf.call( sortInput, a ) - indexOf.call( sortInput, b ) ) :
				0;

		// If the nodes are siblings, we can do a quick check
		} else if ( aup === bup ) {
			return siblingCheck( a, b );
		}

		// Otherwise we need full lists of their ancestors for comparison
		cur = a;
		while ( (cur = cur.parentNode) ) {
			ap.unshift( cur );
		}
		cur = b;
		while ( (cur = cur.parentNode) ) {
			bp.unshift( cur );
		}

		// Walk down the tree looking for a discrepancy
		while ( ap[i] === bp[i] ) {
			i++;
		}

		return i ?
			// Do a sibling check if the nodes have a common ancestor
			siblingCheck( ap[i], bp[i] ) :

			// Otherwise nodes in our document sort first
			ap[i] === preferredDoc ? -1 :
			bp[i] === preferredDoc ? 1 :
			0;
	};

	return doc;
};

Sizzle.matches = function( expr, elements ) {
	return Sizzle( expr, null, null, elements );
};

Sizzle.matchesSelector = function( elem, expr ) {
	// Set document vars if needed
	if ( ( elem.ownerDocument || elem ) !== document ) {
		setDocument( elem );
	}

	// Make sure that attribute selectors are quoted
	expr = expr.replace( rattributeQuotes, "='$1']" );

	if ( support.matchesSelector && documentIsHTML &&
		( !rbuggyMatches || !rbuggyMatches.test( expr ) ) &&
		( !rbuggyQSA     || !rbuggyQSA.test( expr ) ) ) {

		try {
			var ret = matches.call( elem, expr );

			// IE 9's matchesSelector returns false on disconnected nodes
			if ( ret || support.disconnectedMatch ||
					// As well, disconnected nodes are said to be in a document
					// fragment in IE 9
					elem.document && elem.document.nodeType !== 11 ) {
				return ret;
			}
		} catch(e) {}
	}

	return Sizzle( expr, document, null, [elem] ).length > 0;
};

Sizzle.contains = function( context, elem ) {
	// Set document vars if needed
	if ( ( context.ownerDocument || context ) !== document ) {
		setDocument( context );
	}
	return contains( context, elem );
};

Sizzle.attr = function( elem, name ) {
	// Set document vars if needed
	if ( ( elem.ownerDocument || elem ) !== document ) {
		setDocument( elem );
	}

	var fn = Expr.attrHandle[ name.toLowerCase() ],
		// Don't get fooled by Object.prototype properties (jQuery #13807)
		val = fn && hasOwn.call( Expr.attrHandle, name.toLowerCase() ) ?
			fn( elem, name, !documentIsHTML ) :
			undefined;

	return val !== undefined ?
		val :
		support.attributes || !documentIsHTML ?
			elem.getAttribute( name ) :
			(val = elem.getAttributeNode(name)) && val.specified ?
				val.value :
				null;
};

Sizzle.error = function( msg ) {
	throw new Error( "Syntax error, unrecognized expression: " + msg );
};

/**
 * Document sorting and removing duplicates
 * @param {ArrayLike} results
 */
Sizzle.uniqueSort = function( results ) {
	var elem,
		duplicates = [],
		j = 0,
		i = 0;

	// Unless we *know* we can detect duplicates, assume their presence
	hasDuplicate = !support.detectDuplicates;
	sortInput = !support.sortStable && results.slice( 0 );
	results.sort( sortOrder );

	if ( hasDuplicate ) {
		while ( (elem = results[i++]) ) {
			if ( elem === results[ i ] ) {
				j = duplicates.push( i );
			}
		}
		while ( j-- ) {
			results.splice( duplicates[ j ], 1 );
		}
	}

	// Clear input after sorting to release objects
	// See https://github.com/jquery/sizzle/pull/225
	sortInput = null;

	return results;
};

/**
 * Utility function for retrieving the text value of an array of DOM nodes
 * @param {Array|Element} elem
 */
getText = Sizzle.getText = function( elem ) {
	var node,
		ret = "",
		i = 0,
		nodeType = elem.nodeType;

	if ( !nodeType ) {
		// If no nodeType, this is expected to be an array
		while ( (node = elem[i++]) ) {
			// Do not traverse comment nodes
			ret += getText( node );
		}
	} else if ( nodeType === 1 || nodeType === 9 || nodeType === 11 ) {
		// Use textContent for elements
		// innerText usage removed for consistency of new lines (jQuery #11153)
		if ( typeof elem.textContent === "string" ) {
			return elem.textContent;
		} else {
			// Traverse its children
			for ( elem = elem.firstChild; elem; elem = elem.nextSibling ) {
				ret += getText( elem );
			}
		}
	} else if ( nodeType === 3 || nodeType === 4 ) {
		return elem.nodeValue;
	}
	// Do not include comment or processing instruction nodes

	return ret;
};

Expr = Sizzle.selectors = {

	// Can be adjusted by the user
	cacheLength: 50,

	createPseudo: markFunction,

	match: matchExpr,

	attrHandle: {},

	find: {},

	relative: {
		">": { dir: "parentNode", first: true },
		" ": { dir: "parentNode" },
		"+": { dir: "previousSibling", first: true },
		"~": { dir: "previousSibling" }
	},

	preFilter: {
		"ATTR": function( match ) {
			match[1] = match[1].replace( runescape, funescape );

			// Move the given value to match[3] whether quoted or unquoted
			match[3] = ( match[4] || match[5] || "" ).replace( runescape, funescape );

			if ( match[2] === "~=" ) {
				match[3] = " " + match[3] + " ";
			}

			return match.slice( 0, 4 );
		},

		"CHILD": function( match ) {
			/* matches from matchExpr["CHILD"]
				1 type (only|nth|...)
				2 what (child|of-type)
				3 argument (even|odd|\d*|\d*n([+-]\d+)?|...)
				4 xn-component of xn+y argument ([+-]?\d*n|)
				5 sign of xn-component
				6 x of xn-component
				7 sign of y-component
				8 y of y-component
			*/
			match[1] = match[1].toLowerCase();

			if ( match[1].slice( 0, 3 ) === "nth" ) {
				// nth-* requires argument
				if ( !match[3] ) {
					Sizzle.error( match[0] );
				}

				// numeric x and y parameters for Expr.filter.CHILD
				// remember that false/true cast respectively to 0/1
				match[4] = +( match[4] ? match[5] + (match[6] || 1) : 2 * ( match[3] === "even" || match[3] === "odd" ) );
				match[5] = +( ( match[7] + match[8] ) || match[3] === "odd" );

			// other types prohibit arguments
			} else if ( match[3] ) {
				Sizzle.error( match[0] );
			}

			return match;
		},

		"PSEUDO": function( match ) {
			var excess,
				unquoted = !match[5] && match[2];

			if ( matchExpr["CHILD"].test( match[0] ) ) {
				return null;
			}

			// Accept quoted arguments as-is
			if ( match[3] && match[4] !== undefined ) {
				match[2] = match[4];

			// Strip excess characters from unquoted arguments
			} else if ( unquoted && rpseudo.test( unquoted ) &&
				// Get excess from tokenize (recursively)
				(excess = tokenize( unquoted, true )) &&
				// advance to the next closing parenthesis
				(excess = unquoted.indexOf( ")", unquoted.length - excess ) - unquoted.length) ) {

				// excess is a negative index
				match[0] = match[0].slice( 0, excess );
				match[2] = unquoted.slice( 0, excess );
			}

			// Return only captures needed by the pseudo filter method (type and argument)
			return match.slice( 0, 3 );
		}
	},

	filter: {

		"TAG": function( nodeNameSelector ) {
			var nodeName = nodeNameSelector.replace( runescape, funescape ).toLowerCase();
			return nodeNameSelector === "*" ?
				function() { return true; } :
				function( elem ) {
					return elem.nodeName && elem.nodeName.toLowerCase() === nodeName;
				};
		},

		"CLASS": function( className ) {
			var pattern = classCache[ className + " " ];

			return pattern ||
				(pattern = new RegExp( "(^|" + whitespace + ")" + className + "(" + whitespace + "|$)" )) &&
				classCache( className, function( elem ) {
					return pattern.test( typeof elem.className === "string" && elem.className || typeof elem.getAttribute !== strundefined && elem.getAttribute("class") || "" );
				});
		},

		"ATTR": function( name, operator, check ) {
			return function( elem ) {
				var result = Sizzle.attr( elem, name );

				if ( result == null ) {
					return operator === "!=";
				}
				if ( !operator ) {
					return true;
				}

				result += "";

				return operator === "=" ? result === check :
					operator === "!=" ? result !== check :
					operator === "^=" ? check && result.indexOf( check ) === 0 :
					operator === "*=" ? check && result.indexOf( check ) > -1 :
					operator === "$=" ? check && result.slice( -check.length ) === check :
					operator === "~=" ? ( " " + result + " " ).indexOf( check ) > -1 :
					operator === "|=" ? result === check || result.slice( 0, check.length + 1 ) === check + "-" :
					false;
			};
		},

		"CHILD": function( type, what, argument, first, last ) {
			var simple = type.slice( 0, 3 ) !== "nth",
				forward = type.slice( -4 ) !== "last",
				ofType = what === "of-type";

			return first === 1 && last === 0 ?

				// Shortcut for :nth-*(n)
				function( elem ) {
					return !!elem.parentNode;
				} :

				function( elem, context, xml ) {
					var cache, outerCache, node, diff, nodeIndex, start,
						dir = simple !== forward ? "nextSibling" : "previousSibling",
						parent = elem.parentNode,
						name = ofType && elem.nodeName.toLowerCase(),
						useCache = !xml && !ofType;

					if ( parent ) {

						// :(first|last|only)-(child|of-type)
						if ( simple ) {
							while ( dir ) {
								node = elem;
								while ( (node = node[ dir ]) ) {
									if ( ofType ? node.nodeName.toLowerCase() === name : node.nodeType === 1 ) {
										return false;
									}
								}
								// Reverse direction for :only-* (if we haven't yet done so)
								start = dir = type === "only" && !start && "nextSibling";
							}
							return true;
						}

						start = [ forward ? parent.firstChild : parent.lastChild ];

						// non-xml :nth-child(...) stores cache data on `parent`
						if ( forward && useCache ) {
							// Seek `elem` from a previously-cached index
							outerCache = parent[ expando ] || (parent[ expando ] = {});
							cache = outerCache[ type ] || [];
							nodeIndex = cache[0] === dirruns && cache[1];
							diff = cache[0] === dirruns && cache[2];
							node = nodeIndex && parent.childNodes[ nodeIndex ];

							while ( (node = ++nodeIndex && node && node[ dir ] ||

								// Fallback to seeking `elem` from the start
								(diff = nodeIndex = 0) || start.pop()) ) {

								// When found, cache indexes on `parent` and break
								if ( node.nodeType === 1 && ++diff && node === elem ) {
									outerCache[ type ] = [ dirruns, nodeIndex, diff ];
									break;
								}
							}

						// Use previously-cached element index if available
						} else if ( useCache && (cache = (elem[ expando ] || (elem[ expando ] = {}))[ type ]) && cache[0] === dirruns ) {
							diff = cache[1];

						// xml :nth-child(...) or :nth-last-child(...) or :nth(-last)?-of-type(...)
						} else {
							// Use the same loop as above to seek `elem` from the start
							while ( (node = ++nodeIndex && node && node[ dir ] ||
								(diff = nodeIndex = 0) || start.pop()) ) {

								if ( ( ofType ? node.nodeName.toLowerCase() === name : node.nodeType === 1 ) && ++diff ) {
									// Cache the index of each encountered element
									if ( useCache ) {
										(node[ expando ] || (node[ expando ] = {}))[ type ] = [ dirruns, diff ];
									}

									if ( node === elem ) {
										break;
									}
								}
							}
						}

						// Incorporate the offset, then check against cycle size
						diff -= last;
						return diff === first || ( diff % first === 0 && diff / first >= 0 );
					}
				};
		},

		"PSEUDO": function( pseudo, argument ) {
			// pseudo-class names are case-insensitive
			// http://www.w3.org/TR/selectors/#pseudo-classes
			// Prioritize by case sensitivity in case custom pseudos are added with uppercase letters
			// Remember that setFilters inherits from pseudos
			var args,
				fn = Expr.pseudos[ pseudo ] || Expr.setFilters[ pseudo.toLowerCase() ] ||
					Sizzle.error( "unsupported pseudo: " + pseudo );

			// The user may use createPseudo to indicate that
			// arguments are needed to create the filter function
			// just as Sizzle does
			if ( fn[ expando ] ) {
				return fn( argument );
			}

			// But maintain support for old signatures
			if ( fn.length > 1 ) {
				args = [ pseudo, pseudo, "", argument ];
				return Expr.setFilters.hasOwnProperty( pseudo.toLowerCase() ) ?
					markFunction(function( seed, matches ) {
						var idx,
							matched = fn( seed, argument ),
							i = matched.length;
						while ( i-- ) {
							idx = indexOf.call( seed, matched[i] );
							seed[ idx ] = !( matches[ idx ] = matched[i] );
						}
					}) :
					function( elem ) {
						return fn( elem, 0, args );
					};
			}

			return fn;
		}
	},

	pseudos: {
		// Potentially complex pseudos
		"not": markFunction(function( selector ) {
			// Trim the selector passed to compile
			// to avoid treating leading and trailing
			// spaces as combinators
			var input = [],
				results = [],
				matcher = compile( selector.replace( rtrim, "$1" ) );

			return matcher[ expando ] ?
				markFunction(function( seed, matches, context, xml ) {
					var elem,
						unmatched = matcher( seed, null, xml, [] ),
						i = seed.length;

					// Match elements unmatched by `matcher`
					while ( i-- ) {
						if ( (elem = unmatched[i]) ) {
							seed[i] = !(matches[i] = elem);
						}
					}
				}) :
				function( elem, context, xml ) {
					input[0] = elem;
					matcher( input, null, xml, results );
					return !results.pop();
				};
		}),

		"has": markFunction(function( selector ) {
			return function( elem ) {
				return Sizzle( selector, elem ).length > 0;
			};
		}),

		"contains": markFunction(function( text ) {
			return function( elem ) {
				return ( elem.textContent || elem.innerText || getText( elem ) ).indexOf( text ) > -1;
			};
		}),

		// "Whether an element is represented by a :lang() selector
		// is based solely on the element's language value
		// being equal to the identifier C,
		// or beginning with the identifier C immediately followed by "-".
		// The matching of C against the element's language value is performed case-insensitively.
		// The identifier C does not have to be a valid language name."
		// http://www.w3.org/TR/selectors/#lang-pseudo
		"lang": markFunction( function( lang ) {
			// lang value must be a valid identifier
			if ( !ridentifier.test(lang || "") ) {
				Sizzle.error( "unsupported lang: " + lang );
			}
			lang = lang.replace( runescape, funescape ).toLowerCase();
			return function( elem ) {
				var elemLang;
				do {
					if ( (elemLang = documentIsHTML ?
						elem.lang :
						elem.getAttribute("xml:lang") || elem.getAttribute("lang")) ) {

						elemLang = elemLang.toLowerCase();
						return elemLang === lang || elemLang.indexOf( lang + "-" ) === 0;
					}
				} while ( (elem = elem.parentNode) && elem.nodeType === 1 );
				return false;
			};
		}),

		// Miscellaneous
		"target": function( elem ) {
			var hash = window.location && window.location.hash;
			return hash && hash.slice( 1 ) === elem.id;
		},

		"root": function( elem ) {
			return elem === docElem;
		},

		"focus": function( elem ) {
			return elem === document.activeElement && (!document.hasFocus || document.hasFocus()) && !!(elem.type || elem.href || ~elem.tabIndex);
		},

		// Boolean properties
		"enabled": function( elem ) {
			return elem.disabled === false;
		},

		"disabled": function( elem ) {
			return elem.disabled === true;
		},

		"checked": function( elem ) {
			// In CSS3, :checked should return both checked and selected elements
			// http://www.w3.org/TR/2011/REC-css3-selectors-20110929/#checked
			var nodeName = elem.nodeName.toLowerCase();
			return (nodeName === "input" && !!elem.checked) || (nodeName === "option" && !!elem.selected);
		},

		"selected": function( elem ) {
			// Accessing this property makes selected-by-default
			// options in Safari work properly
			if ( elem.parentNode ) {
				elem.parentNode.selectedIndex;
			}

			return elem.selected === true;
		},

		// Contents
		"empty": function( elem ) {
			// http://www.w3.org/TR/selectors/#empty-pseudo
			// :empty is negated by element (1) or content nodes (text: 3; cdata: 4; entity ref: 5),
			//   but not by others (comment: 8; processing instruction: 7; etc.)
			// nodeType < 6 works because attributes (2) do not appear as children
			for ( elem = elem.firstChild; elem; elem = elem.nextSibling ) {
				if ( elem.nodeType < 6 ) {
					return false;
				}
			}
			return true;
		},

		"parent": function( elem ) {
			return !Expr.pseudos["empty"]( elem );
		},

		// Element/input types
		"header": function( elem ) {
			return rheader.test( elem.nodeName );
		},

		"input": function( elem ) {
			return rinputs.test( elem.nodeName );
		},

		"button": function( elem ) {
			var name = elem.nodeName.toLowerCase();
			return name === "input" && elem.type === "button" || name === "button";
		},

		"text": function( elem ) {
			var attr;
			return elem.nodeName.toLowerCase() === "input" &&
				elem.type === "text" &&

				// Support: IE<8
				// New HTML5 attribute values (e.g., "search") appear with elem.type === "text"
				( (attr = elem.getAttribute("type")) == null || attr.toLowerCase() === "text" );
		},

		// Position-in-collection
		"first": createPositionalPseudo(function() {
			return [ 0 ];
		}),

		"last": createPositionalPseudo(function( matchIndexes, length ) {
			return [ length - 1 ];
		}),

		"eq": createPositionalPseudo(function( matchIndexes, length, argument ) {
			return [ argument < 0 ? argument + length : argument ];
		}),

		"even": createPositionalPseudo(function( matchIndexes, length ) {
			var i = 0;
			for ( ; i < length; i += 2 ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		}),

		"odd": createPositionalPseudo(function( matchIndexes, length ) {
			var i = 1;
			for ( ; i < length; i += 2 ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		}),

		"lt": createPositionalPseudo(function( matchIndexes, length, argument ) {
			var i = argument < 0 ? argument + length : argument;
			for ( ; --i >= 0; ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		}),

		"gt": createPositionalPseudo(function( matchIndexes, length, argument ) {
			var i = argument < 0 ? argument + length : argument;
			for ( ; ++i < length; ) {
				matchIndexes.push( i );
			}
			return matchIndexes;
		})
	}
};

Expr.pseudos["nth"] = Expr.pseudos["eq"];

// Add button/input type pseudos
for ( i in { radio: true, checkbox: true, file: true, password: true, image: true } ) {
	Expr.pseudos[ i ] = createInputPseudo( i );
}
for ( i in { submit: true, reset: true } ) {
	Expr.pseudos[ i ] = createButtonPseudo( i );
}

// Easy API for creating new setFilters
function setFilters() {}
setFilters.prototype = Expr.filters = Expr.pseudos;
Expr.setFilters = new setFilters();

function tokenize( selector, parseOnly ) {
	var matched, match, tokens, type,
		soFar, groups, preFilters,
		cached = tokenCache[ selector + " " ];

	if ( cached ) {
		return parseOnly ? 0 : cached.slice( 0 );
	}

	soFar = selector;
	groups = [];
	preFilters = Expr.preFilter;

	while ( soFar ) {

		// Comma and first run
		if ( !matched || (match = rcomma.exec( soFar )) ) {
			if ( match ) {
				// Don't consume trailing commas as valid
				soFar = soFar.slice( match[0].length ) || soFar;
			}
			groups.push( (tokens = []) );
		}

		matched = false;

		// Combinators
		if ( (match = rcombinators.exec( soFar )) ) {
			matched = match.shift();
			tokens.push({
				value: matched,
				// Cast descendant combinators to space
				type: match[0].replace( rtrim, " " )
			});
			soFar = soFar.slice( matched.length );
		}

		// Filters
		for ( type in Expr.filter ) {
			if ( (match = matchExpr[ type ].exec( soFar )) && (!preFilters[ type ] ||
				(match = preFilters[ type ]( match ))) ) {
				matched = match.shift();
				tokens.push({
					value: matched,
					type: type,
					matches: match
				});
				soFar = soFar.slice( matched.length );
			}
		}

		if ( !matched ) {
			break;
		}
	}

	// Return the length of the invalid excess
	// if we're just parsing
	// Otherwise, throw an error or return tokens
	return parseOnly ?
		soFar.length :
		soFar ?
			Sizzle.error( selector ) :
			// Cache the tokens
			tokenCache( selector, groups ).slice( 0 );
}

function toSelector( tokens ) {
	var i = 0,
		len = tokens.length,
		selector = "";
	for ( ; i < len; i++ ) {
		selector += tokens[i].value;
	}
	return selector;
}

function addCombinator( matcher, combinator, base ) {
	var dir = combinator.dir,
		checkNonElements = base && dir === "parentNode",
		doneName = done++;

	return combinator.first ?
		// Check against closest ancestor/preceding element
		function( elem, context, xml ) {
			while ( (elem = elem[ dir ]) ) {
				if ( elem.nodeType === 1 || checkNonElements ) {
					return matcher( elem, context, xml );
				}
			}
		} :

		// Check against all ancestor/preceding elements
		function( elem, context, xml ) {
			var oldCache, outerCache,
				newCache = [ dirruns, doneName ];

			// We can't set arbitrary data on XML nodes, so they don't benefit from dir caching
			if ( xml ) {
				while ( (elem = elem[ dir ]) ) {
					if ( elem.nodeType === 1 || checkNonElements ) {
						if ( matcher( elem, context, xml ) ) {
							return true;
						}
					}
				}
			} else {
				while ( (elem = elem[ dir ]) ) {
					if ( elem.nodeType === 1 || checkNonElements ) {
						outerCache = elem[ expando ] || (elem[ expando ] = {});
						if ( (oldCache = outerCache[ dir ]) &&
							oldCache[ 0 ] === dirruns && oldCache[ 1 ] === doneName ) {

							// Assign to newCache so results back-propagate to previous elements
							return (newCache[ 2 ] = oldCache[ 2 ]);
						} else {
							// Reuse newcache so results back-propagate to previous elements
							outerCache[ dir ] = newCache;

							// A match means we're done; a fail means we have to keep checking
							if ( (newCache[ 2 ] = matcher( elem, context, xml )) ) {
								return true;
							}
						}
					}
				}
			}
		};
}

function elementMatcher( matchers ) {
	return matchers.length > 1 ?
		function( elem, context, xml ) {
			var i = matchers.length;
			while ( i-- ) {
				if ( !matchers[i]( elem, context, xml ) ) {
					return false;
				}
			}
			return true;
		} :
		matchers[0];
}

function condense( unmatched, map, filter, context, xml ) {
	var elem,
		newUnmatched = [],
		i = 0,
		len = unmatched.length,
		mapped = map != null;

	for ( ; i < len; i++ ) {
		if ( (elem = unmatched[i]) ) {
			if ( !filter || filter( elem, context, xml ) ) {
				newUnmatched.push( elem );
				if ( mapped ) {
					map.push( i );
				}
			}
		}
	}

	return newUnmatched;
}

function setMatcher( preFilter, selector, matcher, postFilter, postFinder, postSelector ) {
	if ( postFilter && !postFilter[ expando ] ) {
		postFilter = setMatcher( postFilter );
	}
	if ( postFinder && !postFinder[ expando ] ) {
		postFinder = setMatcher( postFinder, postSelector );
	}
	return markFunction(function( seed, results, context, xml ) {
		var temp, i, elem,
			preMap = [],
			postMap = [],
			preexisting = results.length,

			// Get initial elements from seed or context
			elems = seed || multipleContexts( selector || "*", context.nodeType ? [ context ] : context, [] ),

			// Prefilter to get matcher input, preserving a map for seed-results synchronization
			matcherIn = preFilter && ( seed || !selector ) ?
				condense( elems, preMap, preFilter, context, xml ) :
				elems,

			matcherOut = matcher ?
				// If we have a postFinder, or filtered seed, or non-seed postFilter or preexisting results,
				postFinder || ( seed ? preFilter : preexisting || postFilter ) ?

					// ...intermediate processing is necessary
					[] :

					// ...otherwise use results directly
					results :
				matcherIn;

		// Find primary matches
		if ( matcher ) {
			matcher( matcherIn, matcherOut, context, xml );
		}

		// Apply postFilter
		if ( postFilter ) {
			temp = condense( matcherOut, postMap );
			postFilter( temp, [], context, xml );

			// Un-match failing elements by moving them back to matcherIn
			i = temp.length;
			while ( i-- ) {
				if ( (elem = temp[i]) ) {
					matcherOut[ postMap[i] ] = !(matcherIn[ postMap[i] ] = elem);
				}
			}
		}

		if ( seed ) {
			if ( postFinder || preFilter ) {
				if ( postFinder ) {
					// Get the final matcherOut by condensing this intermediate into postFinder contexts
					temp = [];
					i = matcherOut.length;
					while ( i-- ) {
						if ( (elem = matcherOut[i]) ) {
							// Restore matcherIn since elem is not yet a final match
							temp.push( (matcherIn[i] = elem) );
						}
					}
					postFinder( null, (matcherOut = []), temp, xml );
				}

				// Move matched elements from seed to results to keep them synchronized
				i = matcherOut.length;
				while ( i-- ) {
					if ( (elem = matcherOut[i]) &&
						(temp = postFinder ? indexOf.call( seed, elem ) : preMap[i]) > -1 ) {

						seed[temp] = !(results[temp] = elem);
					}
				}
			}

		// Add elements to results, through postFinder if defined
		} else {
			matcherOut = condense(
				matcherOut === results ?
					matcherOut.splice( preexisting, matcherOut.length ) :
					matcherOut
			);
			if ( postFinder ) {
				postFinder( null, results, matcherOut, xml );
			} else {
				push.apply( results, matcherOut );
			}
		}
	});
}

function matcherFromTokens( tokens ) {
	var checkContext, matcher, j,
		len = tokens.length,
		leadingRelative = Expr.relative[ tokens[0].type ],
		implicitRelative = leadingRelative || Expr.relative[" "],
		i = leadingRelative ? 1 : 0,

		// The foundational matcher ensures that elements are reachable from top-level context(s)
		matchContext = addCombinator( function( elem ) {
			return elem === checkContext;
		}, implicitRelative, true ),
		matchAnyContext = addCombinator( function( elem ) {
			return indexOf.call( checkContext, elem ) > -1;
		}, implicitRelative, true ),
		matchers = [ function( elem, context, xml ) {
			return ( !leadingRelative && ( xml || context !== outermostContext ) ) || (
				(checkContext = context).nodeType ?
					matchContext( elem, context, xml ) :
					matchAnyContext( elem, context, xml ) );
		} ];

	for ( ; i < len; i++ ) {
		if ( (matcher = Expr.relative[ tokens[i].type ]) ) {
			matchers = [ addCombinator(elementMatcher( matchers ), matcher) ];
		} else {
			matcher = Expr.filter[ tokens[i].type ].apply( null, tokens[i].matches );

			// Return special upon seeing a positional matcher
			if ( matcher[ expando ] ) {
				// Find the next relative operator (if any) for proper handling
				j = ++i;
				for ( ; j < len; j++ ) {
					if ( Expr.relative[ tokens[j].type ] ) {
						break;
					}
				}
				return setMatcher(
					i > 1 && elementMatcher( matchers ),
					i > 1 && toSelector(
						// If the preceding token was a descendant combinator, insert an implicit any-element `*`
						tokens.slice( 0, i - 1 ).concat({ value: tokens[ i - 2 ].type === " " ? "*" : "" })
					).replace( rtrim, "$1" ),
					matcher,
					i < j && matcherFromTokens( tokens.slice( i, j ) ),
					j < len && matcherFromTokens( (tokens = tokens.slice( j )) ),
					j < len && toSelector( tokens )
				);
			}
			matchers.push( matcher );
		}
	}

	return elementMatcher( matchers );
}

function matcherFromGroupMatchers( elementMatchers, setMatchers ) {
	var bySet = setMatchers.length > 0,
		byElement = elementMatchers.length > 0,
		superMatcher = function( seed, context, xml, results, outermost ) {
			var elem, j, matcher,
				matchedCount = 0,
				i = "0",
				unmatched = seed && [],
				setMatched = [],
				contextBackup = outermostContext,
				// We must always have either seed elements or outermost context
				elems = seed || byElement && Expr.find["TAG"]( "*", outermost ),
				// Use integer dirruns iff this is the outermost matcher
				dirrunsUnique = (dirruns += contextBackup == null ? 1 : Math.random() || 0.1),
				len = elems.length;

			if ( outermost ) {
				outermostContext = context !== document && context;
			}

			// Add elements passing elementMatchers directly to results
			// Keep `i` a string if there are no elements so `matchedCount` will be "00" below
			// Support: IE<9, Safari
			// Tolerate NodeList properties (IE: "length"; Safari: <number>) matching elements by id
			for ( ; i !== len && (elem = elems[i]) != null; i++ ) {
				if ( byElement && elem ) {
					j = 0;
					while ( (matcher = elementMatchers[j++]) ) {
						if ( matcher( elem, context, xml ) ) {
							results.push( elem );
							break;
						}
					}
					if ( outermost ) {
						dirruns = dirrunsUnique;
					}
				}

				// Track unmatched elements for set filters
				if ( bySet ) {
					// They will have gone through all possible matchers
					if ( (elem = !matcher && elem) ) {
						matchedCount--;
					}

					// Lengthen the array for every element, matched or not
					if ( seed ) {
						unmatched.push( elem );
					}
				}
			}

			// Apply set filters to unmatched elements
			matchedCount += i;
			if ( bySet && i !== matchedCount ) {
				j = 0;
				while ( (matcher = setMatchers[j++]) ) {
					matcher( unmatched, setMatched, context, xml );
				}

				if ( seed ) {
					// Reintegrate element matches to eliminate the need for sorting
					if ( matchedCount > 0 ) {
						while ( i-- ) {
							if ( !(unmatched[i] || setMatched[i]) ) {
								setMatched[i] = pop.call( results );
							}
						}
					}

					// Discard index placeholder values to get only actual matches
					setMatched = condense( setMatched );
				}

				// Add matches to results
				push.apply( results, setMatched );

				// Seedless set matches succeeding multiple successful matchers stipulate sorting
				if ( outermost && !seed && setMatched.length > 0 &&
					( matchedCount + setMatchers.length ) > 1 ) {

					Sizzle.uniqueSort( results );
				}
			}

			// Override manipulation of globals by nested matchers
			if ( outermost ) {
				dirruns = dirrunsUnique;
				outermostContext = contextBackup;
			}

			return unmatched;
		};

	return bySet ?
		markFunction( superMatcher ) :
		superMatcher;
}

compile = Sizzle.compile = function( selector, group /* Internal Use Only */ ) {
	var i,
		setMatchers = [],
		elementMatchers = [],
		cached = compilerCache[ selector + " " ];

	if ( !cached ) {
		// Generate a function of recursive functions that can be used to check each element
		if ( !group ) {
			group = tokenize( selector );
		}
		i = group.length;
		while ( i-- ) {
			cached = matcherFromTokens( group[i] );
			if ( cached[ expando ] ) {
				setMatchers.push( cached );
			} else {
				elementMatchers.push( cached );
			}
		}

		// Cache the compiled function
		cached = compilerCache( selector, matcherFromGroupMatchers( elementMatchers, setMatchers ) );
	}
	return cached;
};

function multipleContexts( selector, contexts, results ) {
	var i = 0,
		len = contexts.length;
	for ( ; i < len; i++ ) {
		Sizzle( selector, contexts[i], results );
	}
	return results;
}

function select( selector, context, results, seed ) {
	var i, tokens, token, type, find,
		match = tokenize( selector );

	if ( !seed ) {
		// Try to minimize operations if there is only one group
		if ( match.length === 1 ) {

			// Take a shortcut and set the context if the root selector is an ID
			tokens = match[0] = match[0].slice( 0 );
			if ( tokens.length > 2 && (token = tokens[0]).type === "ID" &&
					support.getById && context.nodeType === 9 && documentIsHTML &&
					Expr.relative[ tokens[1].type ] ) {

				context = ( Expr.find["ID"]( token.matches[0].replace(runescape, funescape), context ) || [] )[0];
				if ( !context ) {
					return results;
				}
				selector = selector.slice( tokens.shift().value.length );
			}

			// Fetch a seed set for right-to-left matching
			i = matchExpr["needsContext"].test( selector ) ? 0 : tokens.length;
			while ( i-- ) {
				token = tokens[i];

				// Abort if we hit a combinator
				if ( Expr.relative[ (type = token.type) ] ) {
					break;
				}
				if ( (find = Expr.find[ type ]) ) {
					// Search, expanding context for leading sibling combinators
					if ( (seed = find(
						token.matches[0].replace( runescape, funescape ),
						rsibling.test( tokens[0].type ) && testContext( context.parentNode ) || context
					)) ) {

						// If seed is empty or no tokens remain, we can return early
						tokens.splice( i, 1 );
						selector = seed.length && toSelector( tokens );
						if ( !selector ) {
							push.apply( results, seed );
							return results;
						}

						break;
					}
				}
			}
		}
	}

	// Compile and execute a filtering function
	// Provide `match` to avoid retokenization if we modified the selector above
	compile( selector, match )(
		seed,
		context,
		!documentIsHTML,
		results,
		rsibling.test( selector ) && testContext( context.parentNode ) || context
	);
	return results;
}

// One-time assignments

// Sort stability
support.sortStable = expando.split("").sort( sortOrder ).join("") === expando;

// Support: Chrome<14
// Always assume duplicates if they aren't passed to the comparison function
support.detectDuplicates = !!hasDuplicate;

// Initialize against the default document
setDocument();

// Support: Webkit<537.32 - Safari 6.0.3/Chrome 25 (fixed in Chrome 27)
// Detached nodes confoundingly follow *each other*
support.sortDetached = assert(function( div1 ) {
	// Should return 1, but returns 4 (following)
	return div1.compareDocumentPosition( document.createElement("div") ) & 1;
});

// Support: IE<8
// Prevent attribute/property "interpolation"
// http://msdn.microsoft.com/en-us/library/ms536429%28VS.85%29.aspx
if ( !assert(function( div ) {
	div.innerHTML = "<a href='#'></a>";
	return div.firstChild.getAttribute("href") === "#" ;
}) ) {
	addHandle( "type|href|height|width", function( elem, name, isXML ) {
		if ( !isXML ) {
			return elem.getAttribute( name, name.toLowerCase() === "type" ? 1 : 2 );
		}
	});
}

// Support: IE<9
// Use defaultValue in place of getAttribute("value")
if ( !support.attributes || !assert(function( div ) {
	div.innerHTML = "<input/>";
	div.firstChild.setAttribute( "value", "" );
	return div.firstChild.getAttribute( "value" ) === "";
}) ) {
	addHandle( "value", function( elem, name, isXML ) {
		if ( !isXML && elem.nodeName.toLowerCase() === "input" ) {
			return elem.defaultValue;
		}
	});
}

// Support: IE<9
// Use getAttributeNode to fetch booleans when getAttribute lies
if ( !assert(function( div ) {
	return div.getAttribute("disabled") == null;
}) ) {
	addHandle( booleans, function( elem, name, isXML ) {
		var val;
		if ( !isXML ) {
			return elem[ name ] === true ? name.toLowerCase() :
					(val = elem.getAttributeNode( name )) && val.specified ?
					val.value :
				null;
		}
	});
}

return Sizzle;

})( window );



jQuery.find = Sizzle;
jQuery.expr = Sizzle.selectors;
jQuery.expr[":"] = jQuery.expr.pseudos;
jQuery.unique = Sizzle.uniqueSort;
jQuery.text = Sizzle.getText;
jQuery.isXMLDoc = Sizzle.isXML;
jQuery.contains = Sizzle.contains;



var rneedsContext = jQuery.expr.match.needsContext;

var rsingleTag = (/^<(\w+)\s*\/?>(?:<\/\1>|)$/);



var risSimple = /^.[^:#\[\.,]*$/;

// Implement the identical functionality for filter and not
function winnow( elements, qualifier, not ) {
	if ( jQuery.isFunction( qualifier ) ) {
		return jQuery.grep( elements, function( elem, i ) {
			/* jshint -W018 */
			return !!qualifier.call( elem, i, elem ) !== not;
		});

	}

	if ( qualifier.nodeType ) {
		return jQuery.grep( elements, function( elem ) {
			return ( elem === qualifier ) !== not;
		});

	}

	if ( typeof qualifier === "string" ) {
		if ( risSimple.test( qualifier ) ) {
			return jQuery.filter( qualifier, elements, not );
		}

		qualifier = jQuery.filter( qualifier, elements );
	}

	return jQuery.grep( elements, function( elem ) {
		return ( jQuery.inArray( elem, qualifier ) >= 0 ) !== not;
	});
}

jQuery.filter = function( expr, elems, not ) {
	var elem = elems[ 0 ];

	if ( not ) {
		expr = ":not(" + expr + ")";
	}

	return elems.length === 1 && elem.nodeType === 1 ?
		jQuery.find.matchesSelector( elem, expr ) ? [ elem ] : [] :
		jQuery.find.matches( expr, jQuery.grep( elems, function( elem ) {
			return elem.nodeType === 1;
		}));
};

jQuery.fn.extend({
	find: function( selector ) {
		var i,
			ret = [],
			self = this,
			len = self.length;

		if ( typeof selector !== "string" ) {
			return this.pushStack( jQuery( selector ).filter(function() {
				for ( i = 0; i < len; i++ ) {
					if ( jQuery.contains( self[ i ], this ) ) {
						return true;
					}
				}
			}) );
		}

		for ( i = 0; i < len; i++ ) {
			jQuery.find( selector, self[ i ], ret );
		}

		// Needed because $( selector, context ) becomes $( context ).find( selector )
		ret = this.pushStack( len > 1 ? jQuery.unique( ret ) : ret );
		ret.selector = this.selector ? this.selector + " " + selector : selector;
		return ret;
	},
	filter: function( selector ) {
		return this.pushStack( winnow(this, selector || [], false) );
	},
	not: function( selector ) {
		return this.pushStack( winnow(this, selector || [], true) );
	},
	is: function( selector ) {
		return !!winnow(
			this,

			// If this is a positional/relative selector, check membership in the returned set
			// so $("p:first").is("p:last") won't return true for a doc with two "p".
			typeof selector === "string" && rneedsContext.test( selector ) ?
				jQuery( selector ) :
				selector || [],
			false
		).length;
	}
});


// Initialize a jQuery object


// A central reference to the root jQuery(document)
var rootjQuery,

	// Use the correct document accordingly with window argument (sandbox)
	document = window.document,

	// A simple way to check for HTML strings
	// Prioritize #id over <tag> to avoid XSS via location.hash (#9521)
	// Strict HTML recognition (#11290: must start with <)
	rquickExpr = /^(?:\s*(<[\w\W]+>)[^>]*|#([\w-]*))$/,

	init = jQuery.fn.init = function( selector, context ) {
		var match, elem;

		// HANDLE: $(""), $(null), $(undefined), $(false)
		if ( !selector ) {
			return this;
		}

		// Handle HTML strings
		if ( typeof selector === "string" ) {
			if ( selector.charAt(0) === "<" && selector.charAt( selector.length - 1 ) === ">" && selector.length >= 3 ) {
				// Assume that strings that start and end with <> are HTML and skip the regex check
				match = [ null, selector, null ];

			} else {
				match = rquickExpr.exec( selector );
			}

			// Match html or make sure no context is specified for #id
			if ( match && (match[1] || !context) ) {

				// HANDLE: $(html) -> $(array)
				if ( match[1] ) {
					context = context instanceof jQuery ? context[0] : context;

					// scripts is true for back-compat
					// Intentionally let the error be thrown if parseHTML is not present
					jQuery.merge( this, jQuery.parseHTML(
						match[1],
						context && context.nodeType ? context.ownerDocument || context : document,
						true
					) );

					// HANDLE: $(html, props)
					if ( rsingleTag.test( match[1] ) && jQuery.isPlainObject( context ) ) {
						for ( match in context ) {
							// Properties of context are called as methods if possible
							if ( jQuery.isFunction( this[ match ] ) ) {
								this[ match ]( context[ match ] );

							// ...and otherwise set as attributes
							} else {
								this.attr( match, context[ match ] );
							}
						}
					}

					return this;

				// HANDLE: $(#id)
				} else {
					elem = document.getElementById( match[2] );

					// Check parentNode to catch when Blackberry 4.6 returns
					// nodes that are no longer in the document #6963
					if ( elem && elem.parentNode ) {
						// Handle the case where IE and Opera return items
						// by name instead of ID
						if ( elem.id !== match[2] ) {
							return rootjQuery.find( selector );
						}

						// Otherwise, we inject the element directly into the jQuery object
						this.length = 1;
						this[0] = elem;
					}

					this.context = document;
					this.selector = selector;
					return this;
				}

			// HANDLE: $(expr, $(...))
			} else if ( !context || context.jquery ) {
				return ( context || rootjQuery ).find( selector );

			// HANDLE: $(expr, context)
			// (which is just equivalent to: $(context).find(expr)
			} else {
				return this.constructor( context ).find( selector );
			}

		// HANDLE: $(DOMElement)
		} else if ( selector.nodeType ) {
			this.context = this[0] = selector;
			this.length = 1;
			return this;

		// HANDLE: $(function)
		// Shortcut for document ready
		} else if ( jQuery.isFunction( selector ) ) {
			return typeof rootjQuery.ready !== "undefined" ?
				rootjQuery.ready( selector ) :
				// Execute immediately if ready is not present
				selector( jQuery );
		}

		if ( selector.selector !== undefined ) {
			this.selector = selector.selector;
			this.context = selector.context;
		}

		return jQuery.makeArray( selector, this );
	};

// Give the init function the jQuery prototype for later instantiation
init.prototype = jQuery.fn;

// Initialize central reference
rootjQuery = jQuery( document );


var rparentsprev = /^(?:parents|prev(?:Until|All))/,
	// methods guaranteed to produce a unique set when starting from a unique set
	guaranteedUnique = {
		children: true,
		contents: true,
		next: true,
		prev: true
	};

jQuery.extend({
	dir: function( elem, dir, until ) {
		var matched = [],
			cur = elem[ dir ];

		while ( cur && cur.nodeType !== 9 && (until === undefined || cur.nodeType !== 1 || !jQuery( cur ).is( until )) ) {
			if ( cur.nodeType === 1 ) {
				matched.push( cur );
			}
			cur = cur[dir];
		}
		return matched;
	},

	sibling: function( n, elem ) {
		var r = [];

		for ( ; n; n = n.nextSibling ) {
			if ( n.nodeType === 1 && n !== elem ) {
				r.push( n );
			}
		}

		return r;
	}
});

jQuery.fn.extend({
	has: function( target ) {
		var i,
			targets = jQuery( target, this ),
			len = targets.length;

		return this.filter(function() {
			for ( i = 0; i < len; i++ ) {
				if ( jQuery.contains( this, targets[i] ) ) {
					return true;
				}
			}
		});
	},

	closest: function( selectors, context ) {
		var cur,
			i = 0,
			l = this.length,
			matched = [],
			pos = rneedsContext.test( selectors ) || typeof selectors !== "string" ?
				jQuery( selectors, context || this.context ) :
				0;

		for ( ; i < l; i++ ) {
			for ( cur = this[i]; cur && cur !== context; cur = cur.parentNode ) {
				// Always skip document fragments
				if ( cur.nodeType < 11 && (pos ?
					pos.index(cur) > -1 :

					// Don't pass non-elements to Sizzle
					cur.nodeType === 1 &&
						jQuery.find.matchesSelector(cur, selectors)) ) {

					matched.push( cur );
					break;
				}
			}
		}

		return this.pushStack( matched.length > 1 ? jQuery.unique( matched ) : matched );
	},

	// Determine the position of an element within
	// the matched set of elements
	index: function( elem ) {

		// No argument, return index in parent
		if ( !elem ) {
			return ( this[0] && this[0].parentNode ) ? this.first().prevAll().length : -1;
		}

		// index in selector
		if ( typeof elem === "string" ) {
			return jQuery.inArray( this[0], jQuery( elem ) );
		}

		// Locate the position of the desired element
		return jQuery.inArray(
			// If it receives a jQuery object, the first element is used
			elem.jquery ? elem[0] : elem, this );
	},

	add: function( selector, context ) {
		return this.pushStack(
			jQuery.unique(
				jQuery.merge( this.get(), jQuery( selector, context ) )
			)
		);
	},

	addBack: function( selector ) {
		return this.add( selector == null ?
			this.prevObject : this.prevObject.filter(selector)
		);
	}
});

function sibling( cur, dir ) {
	do {
		cur = cur[ dir ];
	} while ( cur && cur.nodeType !== 1 );

	return cur;
}

jQuery.each({
	parent: function( elem ) {
		var parent = elem.parentNode;
		return parent && parent.nodeType !== 11 ? parent : null;
	},
	parents: function( elem ) {
		return jQuery.dir( elem, "parentNode" );
	},
	parentsUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "parentNode", until );
	},
	next: function( elem ) {
		return sibling( elem, "nextSibling" );
	},
	prev: function( elem ) {
		return sibling( elem, "previousSibling" );
	},
	nextAll: function( elem ) {
		return jQuery.dir( elem, "nextSibling" );
	},
	prevAll: function( elem ) {
		return jQuery.dir( elem, "previousSibling" );
	},
	nextUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "nextSibling", until );
	},
	prevUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "previousSibling", until );
	},
	siblings: function( elem ) {
		return jQuery.sibling( ( elem.parentNode || {} ).firstChild, elem );
	},
	children: function( elem ) {
		return jQuery.sibling( elem.firstChild );
	},
	contents: function( elem ) {
		return jQuery.nodeName( elem, "iframe" ) ?
			elem.contentDocument || elem.contentWindow.document :
			jQuery.merge( [], elem.childNodes );
	}
}, function( name, fn ) {
	jQuery.fn[ name ] = function( until, selector ) {
		var ret = jQuery.map( this, fn, until );

		if ( name.slice( -5 ) !== "Until" ) {
			selector = until;
		}

		if ( selector && typeof selector === "string" ) {
			ret = jQuery.filter( selector, ret );
		}

		if ( this.length > 1 ) {
			// Remove duplicates
			if ( !guaranteedUnique[ name ] ) {
				ret = jQuery.unique( ret );
			}

			// Reverse order for parents* and prev-derivatives
			if ( rparentsprev.test( name ) ) {
				ret = ret.reverse();
			}
		}

		return this.pushStack( ret );
	};
});
var rnotwhite = (/\S+/g);



// String to Object options format cache
var optionsCache = {};

// Convert String-formatted options into Object-formatted ones and store in cache
function createOptions( options ) {
	var object = optionsCache[ options ] = {};
	jQuery.each( options.match( rnotwhite ) || [], function( _, flag ) {
		object[ flag ] = true;
	});
	return object;
}

/*
 * Create a callback list using the following parameters:
 *
 *	options: an optional list of space-separated options that will change how
 *			the callback list behaves or a more traditional option object
 *
 * By default a callback list will act like an event callback list and can be
 * "fired" multiple times.
 *
 * Possible options:
 *
 *	once:			will ensure the callback list can only be fired once (like a Deferred)
 *
 *	memory:			will keep track of previous values and will call any callback added
 *					after the list has been fired right away with the latest "memorized"
 *					values (like a Deferred)
 *
 *	unique:			will ensure a callback can only be added once (no duplicate in the list)
 *
 *	stopOnFalse:	interrupt callings when a callback returns false
 *
 */
jQuery.Callbacks = function( options ) {

	// Convert options from String-formatted to Object-formatted if needed
	// (we check in cache first)
	options = typeof options === "string" ?
		( optionsCache[ options ] || createOptions( options ) ) :
		jQuery.extend( {}, options );

	var // Flag to know if list is currently firing
		firing,
		// Last fire value (for non-forgettable lists)
		memory,
		// Flag to know if list was already fired
		fired,
		// End of the loop when firing
		firingLength,
		// Index of currently firing callback (modified by remove if needed)
		firingIndex,
		// First callback to fire (used internally by add and fireWith)
		firingStart,
		// Actual callback list
		list = [],
		// Stack of fire calls for repeatable lists
		stack = !options.once && [],
		// Fire callbacks
		fire = function( data ) {
			memory = options.memory && data;
			fired = true;
			firingIndex = firingStart || 0;
			firingStart = 0;
			firingLength = list.length;
			firing = true;
			for ( ; list && firingIndex < firingLength; firingIndex++ ) {
				if ( list[ firingIndex ].apply( data[ 0 ], data[ 1 ] ) === false && options.stopOnFalse ) {
					memory = false; // To prevent further calls using add
					break;
				}
			}
			firing = false;
			if ( list ) {
				if ( stack ) {
					if ( stack.length ) {
						fire( stack.shift() );
					}
				} else if ( memory ) {
					list = [];
				} else {
					self.disable();
				}
			}
		},
		// Actual Callbacks object
		self = {
			// Add a callback or a collection of callbacks to the list
			add: function() {
				if ( list ) {
					// First, we save the current length
					var start = list.length;
					(function add( args ) {
						jQuery.each( args, function( _, arg ) {
							var type = jQuery.type( arg );
							if ( type === "function" ) {
								if ( !options.unique || !self.has( arg ) ) {
									list.push( arg );
								}
							} else if ( arg && arg.length && type !== "string" ) {
								// Inspect recursively
								add( arg );
							}
						});
					})( arguments );
					// Do we need to add the callbacks to the
					// current firing batch?
					if ( firing ) {
						firingLength = list.length;
					// With memory, if we're not firing then
					// we should call right away
					} else if ( memory ) {
						firingStart = start;
						fire( memory );
					}
				}
				return this;
			},
			// Remove a callback from the list
			remove: function() {
				if ( list ) {
					jQuery.each( arguments, function( _, arg ) {
						var index;
						while ( ( index = jQuery.inArray( arg, list, index ) ) > -1 ) {
							list.splice( index, 1 );
							// Handle firing indexes
							if ( firing ) {
								if ( index <= firingLength ) {
									firingLength--;
								}
								if ( index <= firingIndex ) {
									firingIndex--;
								}
							}
						}
					});
				}
				return this;
			},
			// Check if a given callback is in the list.
			// If no argument is given, return whether or not list has callbacks attached.
			has: function( fn ) {
				return fn ? jQuery.inArray( fn, list ) > -1 : !!( list && list.length );
			},
			// Remove all callbacks from the list
			empty: function() {
				list = [];
				firingLength = 0;
				return this;
			},
			// Have the list do nothing anymore
			disable: function() {
				list = stack = memory = undefined;
				return this;
			},
			// Is it disabled?
			disabled: function() {
				return !list;
			},
			// Lock the list in its current state
			lock: function() {
				stack = undefined;
				if ( !memory ) {
					self.disable();
				}
				return this;
			},
			// Is it locked?
			locked: function() {
				return !stack;
			},
			// Call all callbacks with the given context and arguments
			fireWith: function( context, args ) {
				if ( list && ( !fired || stack ) ) {
					args = args || [];
					args = [ context, args.slice ? args.slice() : args ];
					if ( firing ) {
						stack.push( args );
					} else {
						fire( args );
					}
				}
				return this;
			},
			// Call all the callbacks with the given arguments
			fire: function() {
				self.fireWith( this, arguments );
				return this;
			},
			// To know if the callbacks have already been called at least once
			fired: function() {
				return !!fired;
			}
		};

	return self;
};


jQuery.extend({

	Deferred: function( func ) {
		var tuples = [
				// action, add listener, listener list, final state
				[ "resolve", "done", jQuery.Callbacks("once memory"), "resolved" ],
				[ "reject", "fail", jQuery.Callbacks("once memory"), "rejected" ],
				[ "notify", "progress", jQuery.Callbacks("memory") ]
			],
			state = "pending",
			promise = {
				state: function() {
					return state;
				},
				always: function() {
					deferred.done( arguments ).fail( arguments );
					return this;
				},
				then: function( /* fnDone, fnFail, fnProgress */ ) {
					var fns = arguments;
					return jQuery.Deferred(function( newDefer ) {
						jQuery.each( tuples, function( i, tuple ) {
							var fn = jQuery.isFunction( fns[ i ] ) && fns[ i ];
							// deferred[ done | fail | progress ] for forwarding actions to newDefer
							deferred[ tuple[1] ](function() {
								var returned = fn && fn.apply( this, arguments );
								if ( returned && jQuery.isFunction( returned.promise ) ) {
									returned.promise()
										.done( newDefer.resolve )
										.fail( newDefer.reject )
										.progress( newDefer.notify );
								} else {
									newDefer[ tuple[ 0 ] + "With" ]( this === promise ? newDefer.promise() : this, fn ? [ returned ] : arguments );
								}
							});
						});
						fns = null;
					}).promise();
				},
				// Get a promise for this deferred
				// If obj is provided, the promise aspect is added to the object
				promise: function( obj ) {
					return obj != null ? jQuery.extend( obj, promise ) : promise;
				}
			},
			deferred = {};

		// Keep pipe for back-compat
		promise.pipe = promise.then;

		// Add list-specific methods
		jQuery.each( tuples, function( i, tuple ) {
			var list = tuple[ 2 ],
				stateString = tuple[ 3 ];

			// promise[ done | fail | progress ] = list.add
			promise[ tuple[1] ] = list.add;

			// Handle state
			if ( stateString ) {
				list.add(function() {
					// state = [ resolved | rejected ]
					state = stateString;

				// [ reject_list | resolve_list ].disable; progress_list.lock
				}, tuples[ i ^ 1 ][ 2 ].disable, tuples[ 2 ][ 2 ].lock );
			}

			// deferred[ resolve | reject | notify ]
			deferred[ tuple[0] ] = function() {
				deferred[ tuple[0] + "With" ]( this === deferred ? promise : this, arguments );
				return this;
			};
			deferred[ tuple[0] + "With" ] = list.fireWith;
		});

		// Make the deferred a promise
		promise.promise( deferred );

		// Call given func if any
		if ( func ) {
			func.call( deferred, deferred );
		}

		// All done!
		return deferred;
	},

	// Deferred helper
	when: function( subordinate /* , ..., subordinateN */ ) {
		var i = 0,
			resolveValues = slice.call( arguments ),
			length = resolveValues.length,

			// the count of uncompleted subordinates
			remaining = length !== 1 || ( subordinate && jQuery.isFunction( subordinate.promise ) ) ? length : 0,

			// the master Deferred. If resolveValues consist of only a single Deferred, just use that.
			deferred = remaining === 1 ? subordinate : jQuery.Deferred(),

			// Update function for both resolve and progress values
			updateFunc = function( i, contexts, values ) {
				return function( value ) {
					contexts[ i ] = this;
					values[ i ] = arguments.length > 1 ? slice.call( arguments ) : value;
					if ( values === progressValues ) {
						deferred.notifyWith( contexts, values );

					} else if ( !(--remaining) ) {
						deferred.resolveWith( contexts, values );
					}
				};
			},

			progressValues, progressContexts, resolveContexts;

		// add listeners to Deferred subordinates; treat others as resolved
		if ( length > 1 ) {
			progressValues = new Array( length );
			progressContexts = new Array( length );
			resolveContexts = new Array( length );
			for ( ; i < length; i++ ) {
				if ( resolveValues[ i ] && jQuery.isFunction( resolveValues[ i ].promise ) ) {
					resolveValues[ i ].promise()
						.done( updateFunc( i, resolveContexts, resolveValues ) )
						.fail( deferred.reject )
						.progress( updateFunc( i, progressContexts, progressValues ) );
				} else {
					--remaining;
				}
			}
		}

		// if we're not waiting on anything, resolve the master
		if ( !remaining ) {
			deferred.resolveWith( resolveContexts, resolveValues );
		}

		return deferred.promise();
	}
});


// The deferred used on DOM ready
var readyList;

jQuery.fn.ready = function( fn ) {
	// Add the callback
	jQuery.ready.promise().done( fn );

	return this;
};

jQuery.extend({
	// Is the DOM ready to be used? Set to true once it occurs.
	isReady: false,

	// A counter to track how many items to wait for before
	// the ready event fires. See #6781
	readyWait: 1,

	// Hold (or release) the ready event
	holdReady: function( hold ) {
		if ( hold ) {
			jQuery.readyWait++;
		} else {
			jQuery.ready( true );
		}
	},

	// Handle when the DOM is ready
	ready: function( wait ) {

		// Abort if there are pending holds or we're already ready
		if ( wait === true ? --jQuery.readyWait : jQuery.isReady ) {
			return;
		}

		// Make sure body exists, at least, in case IE gets a little overzealous (ticket #5443).
		if ( !document.body ) {
			return setTimeout( jQuery.ready );
		}

		// Remember that the DOM is ready
		jQuery.isReady = true;

		// If a normal DOM Ready event fired, decrement, and wait if need be
		if ( wait !== true && --jQuery.readyWait > 0 ) {
			return;
		}

		// If there are functions bound, to execute
		readyList.resolveWith( document, [ jQuery ] );

		// Trigger any bound ready events
		if ( jQuery.fn.trigger ) {
			jQuery( document ).trigger("ready").off("ready");
		}
	}
});

/**
 * Clean-up method for dom ready events
 */
function detach() {
	if ( document.addEventListener ) {
		document.removeEventListener( "DOMContentLoaded", completed, false );
		window.removeEventListener( "load", completed, false );

	} else {
		document.detachEvent( "onreadystatechange", completed );
		window.detachEvent( "onload", completed );
	}
}

/**
 * The ready event handler and self cleanup method
 */
function completed() {
	// readyState === "complete" is good enough for us to call the dom ready in oldIE
	if ( document.addEventListener || event.type === "load" || document.readyState === "complete" ) {
		detach();
		jQuery.ready();
	}
}

jQuery.ready.promise = function( obj ) {
	if ( !readyList ) {

		readyList = jQuery.Deferred();

		// Catch cases where $(document).ready() is called after the browser event has already occurred.
		// we once tried to use readyState "interactive" here, but it caused issues like the one
		// discovered by ChrisS here: http://bugs.jquery.com/ticket/12282#comment:15
		if ( document.readyState === "complete" ) {
			// Handle it asynchronously to allow scripts the opportunity to delay ready
			setTimeout( jQuery.ready );

		// Standards-based browsers support DOMContentLoaded
		} else if ( document.addEventListener ) {
			// Use the handy event callback
			document.addEventListener( "DOMContentLoaded", completed, false );

			// A fallback to window.onload, that will always work
			window.addEventListener( "load", completed, false );

		// If IE event model is used
		} else {
			// Ensure firing before onload, maybe late but safe also for iframes
			document.attachEvent( "onreadystatechange", completed );

			// A fallback to window.onload, that will always work
			window.attachEvent( "onload", completed );

			// If IE and not a frame
			// continually check to see if the document is ready
			var top = false;

			try {
				top = window.frameElement == null && document.documentElement;
			} catch(e) {}

			if ( top && top.doScroll ) {
				(function doScrollCheck() {
					if ( !jQuery.isReady ) {

						try {
							// Use the trick by Diego Perini
							// http://javascript.nwbox.com/IEContentLoaded/
							top.doScroll("left");
						} catch(e) {
							return setTimeout( doScrollCheck, 50 );
						}

						// detach all dom ready events
						detach();

						// and execute any waiting functions
						jQuery.ready();
					}
				})();
			}
		}
	}
	return readyList.promise( obj );
};


var strundefined = typeof undefined;



// Support: IE<9
// Iteration over object's inherited properties before its own
var i;
for ( i in jQuery( support ) ) {
	break;
}
support.ownLast = i !== "0";

// Note: most support tests are defined in their respective modules.
// false until the test is run
support.inlineBlockNeedsLayout = false;

jQuery(function() {
	// We need to execute this one support test ASAP because we need to know
	// if body.style.zoom needs to be set.

	var container, div,
		body = document.getElementsByTagName("body")[0];

	if ( !body ) {
		// Return for frameset docs that don't have a body
		return;
	}

	// Setup
	container = document.createElement( "div" );
	container.style.cssText = "border:0;width:0;height:0;position:absolute;top:0;left:-9999px;margin-top:1px";

	div = document.createElement( "div" );
	body.appendChild( container ).appendChild( div );

	if ( typeof div.style.zoom !== strundefined ) {
		// Support: IE<8
		// Check if natively block-level elements act like inline-block
		// elements when setting their display to 'inline' and giving
		// them layout
		div.style.cssText = "border:0;margin:0;width:1px;padding:1px;display:inline;zoom:1";

		if ( (support.inlineBlockNeedsLayout = ( div.offsetWidth === 3 )) ) {
			// Prevent IE 6 from affecting layout for positioned elements #11048
			// Prevent IE from shrinking the body in IE 7 mode #12869
			// Support: IE<8
			body.style.zoom = 1;
		}
	}

	body.removeChild( container );

	// Null elements to avoid leaks in IE
	container = div = null;
});




(function() {
	var div = document.createElement( "div" );

	// Execute the test only if not already executed in another module.
	if (support.deleteExpando == null) {
		// Support: IE<9
		support.deleteExpando = true;
		try {
			delete div.test;
		} catch( e ) {
			support.deleteExpando = false;
		}
	}

	// Null elements to avoid leaks in IE.
	div = null;
})();


/**
 * Determines whether an object can have data
 */
jQuery.acceptData = function( elem ) {
	var noData = jQuery.noData[ (elem.nodeName + " ").toLowerCase() ],
		nodeType = +elem.nodeType || 1;

	// Do not set data on non-element DOM nodes because it will not be cleared (#8335).
	return nodeType !== 1 && nodeType !== 9 ?
		false :

		// Nodes accept data unless otherwise specified; rejection can be conditional
		!noData || noData !== true && elem.getAttribute("classid") === noData;
};


var rbrace = /^(?:\{[\w\W]*\}|\[[\w\W]*\])$/,
	rmultiDash = /([A-Z])/g;

function dataAttr( elem, key, data ) {
	// If nothing was found internally, try to fetch any
	// data from the HTML5 data-* attribute
	if ( data === undefined && elem.nodeType === 1 ) {

		var name = "data-" + key.replace( rmultiDash, "-$1" ).toLowerCase();

		data = elem.getAttribute( name );

		if ( typeof data === "string" ) {
			try {
				data = data === "true" ? true :
					data === "false" ? false :
					data === "null" ? null :
					// Only convert to a number if it doesn't change the string
					+data + "" === data ? +data :
					rbrace.test( data ) ? jQuery.parseJSON( data ) :
					data;
			} catch( e ) {}

			// Make sure we set the data so it isn't changed later
			jQuery.data( elem, key, data );

		} else {
			data = undefined;
		}
	}

	return data;
}

// checks a cache object for emptiness
function isEmptyDataObject( obj ) {
	var name;
	for ( name in obj ) {

		// if the public data object is empty, the private is still empty
		if ( name === "data" && jQuery.isEmptyObject( obj[name] ) ) {
			continue;
		}
		if ( name !== "toJSON" ) {
			return false;
		}
	}

	return true;
}

function internalData( elem, name, data, pvt /* Internal Use Only */ ) {
	if ( !jQuery.acceptData( elem ) ) {
		return;
	}

	var ret, thisCache,
		internalKey = jQuery.expando,

		// We have to handle DOM nodes and JS objects differently because IE6-7
		// can't GC object references properly across the DOM-JS boundary
		isNode = elem.nodeType,

		// Only DOM nodes need the global jQuery cache; JS object data is
		// attached directly to the object so GC can occur automatically
		cache = isNode ? jQuery.cache : elem,

		// Only defining an ID for JS objects if its cache already exists allows
		// the code to shortcut on the same path as a DOM node with no cache
		id = isNode ? elem[ internalKey ] : elem[ internalKey ] && internalKey;

	// Avoid doing any more work than we need to when trying to get data on an
	// object that has no data at all
	if ( (!id || !cache[id] || (!pvt && !cache[id].data)) && data === undefined && typeof name === "string" ) {
		return;
	}

	if ( !id ) {
		// Only DOM nodes need a new unique ID for each element since their data
		// ends up in the global cache
		if ( isNode ) {
			id = elem[ internalKey ] = deletedIds.pop() || jQuery.guid++;
		} else {
			id = internalKey;
		}
	}

	if ( !cache[ id ] ) {
		// Avoid exposing jQuery metadata on plain JS objects when the object
		// is serialized using JSON.stringify
		cache[ id ] = isNode ? {} : { toJSON: jQuery.noop };
	}

	// An object can be passed to jQuery.data instead of a key/value pair; this gets
	// shallow copied over onto the existing cache
	if ( typeof name === "object" || typeof name === "function" ) {
		if ( pvt ) {
			cache[ id ] = jQuery.extend( cache[ id ], name );
		} else {
			cache[ id ].data = jQuery.extend( cache[ id ].data, name );
		}
	}

	thisCache = cache[ id ];

	// jQuery data() is stored in a separate object inside the object's internal data
	// cache in order to avoid key collisions between internal data and user-defined
	// data.
	if ( !pvt ) {
		if ( !thisCache.data ) {
			thisCache.data = {};
		}

		thisCache = thisCache.data;
	}

	if ( data !== undefined ) {
		thisCache[ jQuery.camelCase( name ) ] = data;
	}

	// Check for both converted-to-camel and non-converted data property names
	// If a data property was specified
	if ( typeof name === "string" ) {

		// First Try to find as-is property data
		ret = thisCache[ name ];

		// Test for null|undefined property data
		if ( ret == null ) {

			// Try to find the camelCased property
			ret = thisCache[ jQuery.camelCase( name ) ];
		}
	} else {
		ret = thisCache;
	}

	return ret;
}

function internalRemoveData( elem, name, pvt ) {
	if ( !jQuery.acceptData( elem ) ) {
		return;
	}

	var thisCache, i,
		isNode = elem.nodeType,

		// See jQuery.data for more information
		cache = isNode ? jQuery.cache : elem,
		id = isNode ? elem[ jQuery.expando ] : jQuery.expando;

	// If there is already no cache entry for this object, there is no
	// purpose in continuing
	if ( !cache[ id ] ) {
		return;
	}

	if ( name ) {

		thisCache = pvt ? cache[ id ] : cache[ id ].data;

		if ( thisCache ) {

			// Support array or space separated string names for data keys
			if ( !jQuery.isArray( name ) ) {

				// try the string as a key before any manipulation
				if ( name in thisCache ) {
					name = [ name ];
				} else {

					// split the camel cased version by spaces unless a key with the spaces exists
					name = jQuery.camelCase( name );
					if ( name in thisCache ) {
						name = [ name ];
					} else {
						name = name.split(" ");
					}
				}
			} else {
				// If "name" is an array of keys...
				// When data is initially created, via ("key", "val") signature,
				// keys will be converted to camelCase.
				// Since there is no way to tell _how_ a key was added, remove
				// both plain key and camelCase key. #12786
				// This will only penalize the array argument path.
				name = name.concat( jQuery.map( name, jQuery.camelCase ) );
			}

			i = name.length;
			while ( i-- ) {
				delete thisCache[ name[i] ];
			}

			// If there is no data left in the cache, we want to continue
			// and let the cache object itself get destroyed
			if ( pvt ? !isEmptyDataObject(thisCache) : !jQuery.isEmptyObject(thisCache) ) {
				return;
			}
		}
	}

	// See jQuery.data for more information
	if ( !pvt ) {
		delete cache[ id ].data;

		// Don't destroy the parent cache unless the internal data object
		// had been the only thing left in it
		if ( !isEmptyDataObject( cache[ id ] ) ) {
			return;
		}
	}

	// Destroy the cache
	if ( isNode ) {
		jQuery.cleanData( [ elem ], true );

	// Use delete when supported for expandos or `cache` is not a window per isWindow (#10080)
	/* jshint eqeqeq: false */
	} else if ( support.deleteExpando || cache != cache.window ) {
		/* jshint eqeqeq: true */
		delete cache[ id ];

	// When all else fails, null
	} else {
		cache[ id ] = null;
	}
}

jQuery.extend({
	cache: {},

	// The following elements (space-suffixed to avoid Object.prototype collisions)
	// throw uncatchable exceptions if you attempt to set expando properties
	noData: {
		"applet ": true,
		"embed ": true,
		// ...but Flash objects (which have this classid) *can* handle expandos
		"object ": "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
	},

	hasData: function( elem ) {
		elem = elem.nodeType ? jQuery.cache[ elem[jQuery.expando] ] : elem[ jQuery.expando ];
		return !!elem && !isEmptyDataObject( elem );
	},

	data: function( elem, name, data ) {
		return internalData( elem, name, data );
	},

	removeData: function( elem, name ) {
		return internalRemoveData( elem, name );
	},

	// For internal use only.
	_data: function( elem, name, data ) {
		return internalData( elem, name, data, true );
	},

	_removeData: function( elem, name ) {
		return internalRemoveData( elem, name, true );
	}
});

jQuery.fn.extend({
	data: function( key, value ) {
		var i, name, data,
			elem = this[0],
			attrs = elem && elem.attributes;

		// Special expections of .data basically thwart jQuery.access,
		// so implement the relevant behavior ourselves

		// Gets all values
		if ( key === undefined ) {
			if ( this.length ) {
				data = jQuery.data( elem );

				if ( elem.nodeType === 1 && !jQuery._data( elem, "parsedAttrs" ) ) {
					i = attrs.length;
					while ( i-- ) {
						name = attrs[i].name;

						if ( name.indexOf("data-") === 0 ) {
							name = jQuery.camelCase( name.slice(5) );

							dataAttr( elem, name, data[ name ] );
						}
					}
					jQuery._data( elem, "parsedAttrs", true );
				}
			}

			return data;
		}

		// Sets multiple values
		if ( typeof key === "object" ) {
			return this.each(function() {
				jQuery.data( this, key );
			});
		}

		return arguments.length > 1 ?

			// Sets one value
			this.each(function() {
				jQuery.data( this, key, value );
			}) :

			// Gets one value
			// Try to fetch any internally stored data first
			elem ? dataAttr( elem, key, jQuery.data( elem, key ) ) : undefined;
	},

	removeData: function( key ) {
		return this.each(function() {
			jQuery.removeData( this, key );
		});
	}
});


jQuery.extend({
	queue: function( elem, type, data ) {
		var queue;

		if ( elem ) {
			type = ( type || "fx" ) + "queue";
			queue = jQuery._data( elem, type );

			// Speed up dequeue by getting out quickly if this is just a lookup
			if ( data ) {
				if ( !queue || jQuery.isArray(data) ) {
					queue = jQuery._data( elem, type, jQuery.makeArray(data) );
				} else {
					queue.push( data );
				}
			}
			return queue || [];
		}
	},

	dequeue: function( elem, type ) {
		type = type || "fx";

		var queue = jQuery.queue( elem, type ),
			startLength = queue.length,
			fn = queue.shift(),
			hooks = jQuery._queueHooks( elem, type ),
			next = function() {
				jQuery.dequeue( elem, type );
			};

		// If the fx queue is dequeued, always remove the progress sentinel
		if ( fn === "inprogress" ) {
			fn = queue.shift();
			startLength--;
		}

		if ( fn ) {

			// Add a progress sentinel to prevent the fx queue from being
			// automatically dequeued
			if ( type === "fx" ) {
				queue.unshift( "inprogress" );
			}

			// clear up the last queue stop function
			delete hooks.stop;
			fn.call( elem, next, hooks );
		}

		if ( !startLength && hooks ) {
			hooks.empty.fire();
		}
	},

	// not intended for public consumption - generates a queueHooks object, or returns the current one
	_queueHooks: function( elem, type ) {
		var key = type + "queueHooks";
		return jQuery._data( elem, key ) || jQuery._data( elem, key, {
			empty: jQuery.Callbacks("once memory").add(function() {
				jQuery._removeData( elem, type + "queue" );
				jQuery._removeData( elem, key );
			})
		});
	}
});

jQuery.fn.extend({
	queue: function( type, data ) {
		var setter = 2;

		if ( typeof type !== "string" ) {
			data = type;
			type = "fx";
			setter--;
		}

		if ( arguments.length < setter ) {
			return jQuery.queue( this[0], type );
		}

		return data === undefined ?
			this :
			this.each(function() {
				var queue = jQuery.queue( this, type, data );

				// ensure a hooks for this queue
				jQuery._queueHooks( this, type );

				if ( type === "fx" && queue[0] !== "inprogress" ) {
					jQuery.dequeue( this, type );
				}
			});
	},
	dequeue: function( type ) {
		return this.each(function() {
			jQuery.dequeue( this, type );
		});
	},
	clearQueue: function( type ) {
		return this.queue( type || "fx", [] );
	},
	// Get a promise resolved when queues of a certain type
	// are emptied (fx is the type by default)
	promise: function( type, obj ) {
		var tmp,
			count = 1,
			defer = jQuery.Deferred(),
			elements = this,
			i = this.length,
			resolve = function() {
				if ( !( --count ) ) {
					defer.resolveWith( elements, [ elements ] );
				}
			};

		if ( typeof type !== "string" ) {
			obj = type;
			type = undefined;
		}
		type = type || "fx";

		while ( i-- ) {
			tmp = jQuery._data( elements[ i ], type + "queueHooks" );
			if ( tmp && tmp.empty ) {
				count++;
				tmp.empty.add( resolve );
			}
		}
		resolve();
		return defer.promise( obj );
	}
});
var pnum = (/[+-]?(?:\d*\.|)\d+(?:[eE][+-]?\d+|)/).source;

var cssExpand = [ "Top", "Right", "Bottom", "Left" ];

var isHidden = function( elem, el ) {
		// isHidden might be called from jQuery#filter function;
		// in that case, element will be second argument
		elem = el || elem;
		return jQuery.css( elem, "display" ) === "none" || !jQuery.contains( elem.ownerDocument, elem );
	};



// Multifunctional method to get and set values of a collection
// The value/s can optionally be executed if it's a function
var access = jQuery.access = function( elems, fn, key, value, chainable, emptyGet, raw ) {
	var i = 0,
		length = elems.length,
		bulk = key == null;

	// Sets many values
	if ( jQuery.type( key ) === "object" ) {
		chainable = true;
		for ( i in key ) {
			jQuery.access( elems, fn, i, key[i], true, emptyGet, raw );
		}

	// Sets one value
	} else if ( value !== undefined ) {
		chainable = true;

		if ( !jQuery.isFunction( value ) ) {
			raw = true;
		}

		if ( bulk ) {
			// Bulk operations run against the entire set
			if ( raw ) {
				fn.call( elems, value );
				fn = null;

			// ...except when executing function values
			} else {
				bulk = fn;
				fn = function( elem, key, value ) {
					return bulk.call( jQuery( elem ), value );
				};
			}
		}

		if ( fn ) {
			for ( ; i < length; i++ ) {
				fn( elems[i], key, raw ? value : value.call( elems[i], i, fn( elems[i], key ) ) );
			}
		}
	}

	return chainable ?
		elems :

		// Gets
		bulk ?
			fn.call( elems ) :
			length ? fn( elems[0], key ) : emptyGet;
};
var rcheckableType = (/^(?:checkbox|radio)$/i);



(function() {
	var fragment = document.createDocumentFragment(),
		div = document.createElement("div"),
		input = document.createElement("input");

	// Setup
	div.setAttribute( "className", "t" );
	div.innerHTML = "  <link/><table></table><a href='/a'>a</a>";

	// IE strips leading whitespace when .innerHTML is used
	support.leadingWhitespace = div.firstChild.nodeType === 3;

	// Make sure that tbody elements aren't automatically inserted
	// IE will insert them into empty tables
	support.tbody = !div.getElementsByTagName( "tbody" ).length;

	// Make sure that link elements get serialized correctly by innerHTML
	// This requires a wrapper element in IE
	support.htmlSerialize = !!div.getElementsByTagName( "link" ).length;

	// Makes sure cloning an html5 element does not cause problems
	// Where outerHTML is undefined, this still works
	support.html5Clone =
		document.createElement( "nav" ).cloneNode( true ).outerHTML !== "<:nav></:nav>";

	// Check if a disconnected checkbox will retain its checked
	// value of true after appended to the DOM (IE6/7)
	input.type = "checkbox";
	input.checked = true;
	fragment.appendChild( input );
	support.appendChecked = input.checked;

	// Make sure textarea (and checkbox) defaultValue is properly cloned
	// Support: IE6-IE11+
	div.innerHTML = "<textarea>x</textarea>";
	support.noCloneChecked = !!div.cloneNode( true ).lastChild.defaultValue;

	// #11217 - WebKit loses check when the name is after the checked attribute
	fragment.appendChild( div );
	div.innerHTML = "<input type='radio' checked='checked' name='t'/>";

	// Support: Safari 5.1, iOS 5.1, Android 4.x, Android 2.3
	// old WebKit doesn't clone checked state correctly in fragments
	support.checkClone = div.cloneNode( true ).cloneNode( true ).lastChild.checked;

	// Support: IE<9
	// Opera does not clone events (and typeof div.attachEvent === undefined).
	// IE9-10 clones events bound via attachEvent, but they don't trigger with .click()
	support.noCloneEvent = true;
	if ( div.attachEvent ) {
		div.attachEvent( "onclick", function() {
			support.noCloneEvent = false;
		});

		div.cloneNode( true ).click();
	}

	// Execute the test only if not already executed in another module.
	if (support.deleteExpando == null) {
		// Support: IE<9
		support.deleteExpando = true;
		try {
			delete div.test;
		} catch( e ) {
			support.deleteExpando = false;
		}
	}

	// Null elements to avoid leaks in IE.
	fragment = div = input = null;
})();


(function() {
	var i, eventName,
		div = document.createElement( "div" );

	// Support: IE<9 (lack submit/change bubble), Firefox 23+ (lack focusin event)
	for ( i in { submit: true, change: true, focusin: true }) {
		eventName = "on" + i;

		if ( !(support[ i + "Bubbles" ] = eventName in window) ) {
			// Beware of CSP restrictions (https://developer.mozilla.org/en/Security/CSP)
			div.setAttribute( eventName, "t" );
			support[ i + "Bubbles" ] = div.attributes[ eventName ].expando === false;
		}
	}

	// Null elements to avoid leaks in IE.
	div = null;
})();


var rformElems = /^(?:input|select|textarea)$/i,
	rkeyEvent = /^key/,
	rmouseEvent = /^(?:mouse|pointer|contextmenu)|click/, // BHTEST
	rfocusMorph = /^(?:focusinfocus|focusoutblur)$/,
	rtypenamespace = /^([^.]*)(?:\.(.+)|)$/;

function returnTrue() {
	return true;
}

function returnFalse() {
	return false;
}

function safeActiveElement() {
	try {
		return document.activeElement;
	} catch ( err ) { }
}

/*
 * Helper functions for managing events -- not part of the public interface.
 * Props to Dean Edwards' addEvent library for many of the ideas.
 */
jQuery.event = {

	global: {},

	add: function( elem, types, handler, data, selector ) {
		var tmp, events, t, handleObjIn,
			special, eventHandle, handleObj,
			handlers, type, namespaces, origType,
			elemData = jQuery._data( elem );

		// Don't attach events to noData or text/comment nodes (but allow plain objects)
		if ( !elemData ) {
			return;
		}

		// Caller can pass in an object of custom data in lieu of the handler
		if ( handler.handler ) {
			handleObjIn = handler;
			handler = handleObjIn.handler;
			selector = handleObjIn.selector;
		}

		// Make sure that the handler has a unique ID, used to find/remove it later
		if ( !handler.guid ) {
			handler.guid = jQuery.guid++;
		}

		// Init the element's event structure and main handler, if this is the first
		if ( !(events = elemData.events) ) {
			events = elemData.events = {};
		}
		if ( !(eventHandle = elemData.handle) ) {
			eventHandle = elemData.handle = function( e ) {
				// Discard the second event of a jQuery.event.trigger() and
				// when an event is called after a page has unloaded
				return typeof jQuery !== strundefined && (!e || jQuery.event.triggered !== e.type) ?
					jQuery.event.dispatch.apply( eventHandle.elem, arguments ) :
					undefined;
			};
			// Add elem as a property of the handle fn to prevent a memory leak with IE non-native events
			eventHandle.elem = elem;
		}

		// Handle multiple events separated by a space
		types = ( types || "" ).match( rnotwhite ) || [ "" ];
		t = types.length;
		while ( t-- ) {
			tmp = rtypenamespace.exec( types[t] ) || [];
			type = origType = tmp[1];
			namespaces = ( tmp[2] || "" ).split( "." ).sort();

			// There *must* be a type, no attaching namespace-only handlers
			if ( !type ) {
				continue;
			}

			// If event changes its type, use the special event handlers for the changed type
			special = jQuery.event.special[ type ] || {};

			// If selector defined, determine special event api type, otherwise given type
			type = ( selector ? special.delegateType : special.bindType ) || type;

			// Update special based on newly reset type
			special = jQuery.event.special[ type ] || {};

			// handleObj is passed to all event handlers
			handleObj = jQuery.extend({
				type: type,
				origType: origType,
				data: data,
				handler: handler,
				guid: handler.guid,
				selector: selector,
				needsContext: selector && jQuery.expr.match.needsContext.test( selector ),
				namespace: namespaces.join(".")
			}, handleObjIn );

			// Init the event handler queue if we're the first
			if ( !(handlers = events[ type ]) ) {
				handlers = events[ type ] = [];
				handlers.delegateCount = 0;

				// Only use addEventListener/attachEvent if the special events handler returns false
				if ( !special.setup || special.setup.call( elem, data, namespaces, eventHandle ) === false ) {
					// Bind the global event handler to the element
					if ( elem.addEventListener ) {
						elem.addEventListener( type, eventHandle, false );

					} else if ( elem.attachEvent ) {
						elem.attachEvent( "on" + type, eventHandle );
					}
				}
			}

			if ( special.add ) {
				special.add.call( elem, handleObj );

				if ( !handleObj.handler.guid ) {
					handleObj.handler.guid = handler.guid;
				}
			}

			// Add to the element's handler list, delegates in front
			if ( selector ) {
				handlers.splice( handlers.delegateCount++, 0, handleObj );
			} else {
				handlers.push( handleObj );
			}

			// Keep track of which events have ever been used, for event optimization
			jQuery.event.global[ type ] = true;
		}

		// Nullify elem to prevent memory leaks in IE
		elem = null;
	},

	// Detach an event or set of events from an element
	remove: function( elem, types, handler, selector, mappedTypes ) {
		var j, handleObj, tmp,
			origCount, t, events,
			special, handlers, type,
			namespaces, origType,
			elemData = jQuery.hasData( elem ) && jQuery._data( elem );

		if ( !elemData || !(events = elemData.events) ) {
			return;
		}

		// Once for each type.namespace in types; type may be omitted
		types = ( types || "" ).match( rnotwhite ) || [ "" ];
		t = types.length;
		while ( t-- ) {
			tmp = rtypenamespace.exec( types[t] ) || [];
			type = origType = tmp[1];
			namespaces = ( tmp[2] || "" ).split( "." ).sort();

			// Unbind all events (on this namespace, if provided) for the element
			if ( !type ) {
				for ( type in events ) {
					jQuery.event.remove( elem, type + types[ t ], handler, selector, true );
				}
				continue;
			}

			special = jQuery.event.special[ type ] || {};
			type = ( selector ? special.delegateType : special.bindType ) || type;
			handlers = events[ type ] || [];
			tmp = tmp[2] && new RegExp( "(^|\\.)" + namespaces.join("\\.(?:.*\\.|)") + "(\\.|$)" );

			// Remove matching events
			origCount = j = handlers.length;
			while ( j-- ) {
				handleObj = handlers[ j ];

				if ( ( mappedTypes || origType === handleObj.origType ) &&
					( !handler || handler.guid === handleObj.guid ) &&
					( !tmp || tmp.test( handleObj.namespace ) ) &&
					( !selector || selector === handleObj.selector || selector === "**" && handleObj.selector ) ) {
					handlers.splice( j, 1 );

					if ( handleObj.selector ) {
						handlers.delegateCount--;
					}
					if ( special.remove ) {
						special.remove.call( elem, handleObj );
					}
				}
			}

			// Remove generic event handler if we removed something and no more handlers exist
			// (avoids potential for endless recursion during removal of special event handlers)
			if ( origCount && !handlers.length ) {
				if ( !special.teardown || special.teardown.call( elem, namespaces, elemData.handle ) === false ) {
					jQuery.removeEvent( elem, type, elemData.handle );
				}

				delete events[ type ];
			}
		}

		// Remove the expando if it's no longer used
		if ( jQuery.isEmptyObject( events ) ) {
			delete elemData.handle;

			// removeData also checks for emptiness and clears the expando if empty
			// so use it instead of delete
			jQuery._removeData( elem, "events" );
		}
	},

	trigger: function( event, data, elem, onlyHandlers ) {
		var handle, ontype, cur,
			bubbleType, special, tmp, i,
			eventPath = [ elem || document ],
			type = hasOwn.call( event, "type" ) ? event.type : event,
			namespaces = hasOwn.call( event, "namespace" ) ? event.namespace.split(".") : [];

		cur = tmp = elem = elem || document;

		// Don't do events on text and comment nodes
		if ( elem.nodeType === 3 || elem.nodeType === 8 ) {
			return;
		}

		// focus/blur morphs to focusin/out; ensure we're not firing them right now
		if ( rfocusMorph.test( type + jQuery.event.triggered ) ) {
			return;
		}

		if ( type.indexOf(".") >= 0 ) {
			// Namespaced trigger; create a regexp to match event type in handle()
			namespaces = type.split(".");
			type = namespaces.shift();
			namespaces.sort();
		}
		ontype = type.indexOf(":") < 0 && "on" + type;

		// Caller can pass in a jQuery.Event object, Object, or just an event type string
		event = event[ jQuery.expando ] ?
			event :
			new jQuery.Event( type, typeof event === "object" && event );

		// Trigger bitmask: & 1 for native handlers; & 2 for jQuery (always true)
		event.isTrigger = onlyHandlers ? 2 : 3;
		event.namespace = namespaces.join(".");
		event.namespace_re = event.namespace ?
			new RegExp( "(^|\\.)" + namespaces.join("\\.(?:.*\\.|)") + "(\\.|$)" ) :
			null;

		// Clean up the event in case it is being reused
		event.result = undefined;
		if ( !event.target ) {
			event.target = elem;
		}

		// Clone any incoming data and prepend the event, creating the handler arg list
		data = data == null ?
			[ event ] :
			jQuery.makeArray( data, [ event ] );

		// Allow special events to draw outside the lines
		special = jQuery.event.special[ type ] || {};
		if ( !onlyHandlers && special.trigger && special.trigger.apply( elem, data ) === false ) {
			return;
		}

		// Determine event propagation path in advance, per W3C events spec (#9951)
		// Bubble up to document, then to window; watch for a global ownerDocument var (#9724)
		if ( !onlyHandlers && !special.noBubble && !jQuery.isWindow( elem ) ) {

			bubbleType = special.delegateType || type;
			if ( !rfocusMorph.test( bubbleType + type ) ) {
				cur = cur.parentNode;
			}
			for ( ; cur; cur = cur.parentNode ) {
				eventPath.push( cur );
				tmp = cur;
			}

			// Only add window if we got to document (e.g., not plain obj or detached DOM)
			if ( tmp === (elem.ownerDocument || document) ) {
				eventPath.push( tmp.defaultView || tmp.parentWindow || window );
			}
		}

		// Fire handlers on the event path
		i = 0;
		while ( (cur = eventPath[i++]) && !event.isPropagationStopped() ) {

			event.type = i > 1 ?
				bubbleType :
				special.bindType || type;

			// jQuery handler
			handle = ( jQuery._data( cur, "events" ) || {} )[ event.type ] && jQuery._data( cur, "handle" );
			if ( handle ) {
				handle.apply( cur, data );
			}

			// Native handler
			handle = ontype && cur[ ontype ];
			if ( handle && handle.apply && jQuery.acceptData( cur ) ) {
				event.result = handle.apply( cur, data );
				if ( event.result === false ) {
					event.preventDefault();
				}
			}
		}
		event.type = type;

		// If nobody prevented the default action, do it now
		if ( !onlyHandlers && !event.isDefaultPrevented() ) {

			if ( (!special._default || special._default.apply( eventPath.pop(), data ) === false) &&
				jQuery.acceptData( elem ) ) {

				// Call a native DOM method on the target with the same name name as the event.
				// Can't use an .isFunction() check here because IE6/7 fails that test.
				// Don't do default actions on window, that's where global variables be (#6170)
				if ( ontype && elem[ type ] && !jQuery.isWindow( elem ) ) {

					// Don't re-trigger an onFOO event when we call its FOO() method
					tmp = elem[ ontype ];

					if ( tmp ) {
						elem[ ontype ] = null;
					}

					// Prevent re-triggering of the same event, since we already bubbled it above
					jQuery.event.triggered = type;
					try {
						elem[ type ]();
					} catch ( e ) {
						// IE<9 dies on focus/blur to hidden element (#1486,#12518)
						// only reproducible on winXP IE8 native, not IE9 in IE8 mode
					}
					jQuery.event.triggered = undefined;

					if ( tmp ) {
						elem[ ontype ] = tmp;
					}
				}
			}
		}

		return event.result;
	},

	dispatch: function( event ) {

		// Make a writable jQuery.Event from the native event object
		event = jQuery.event.fix( event );

		var i, ret, handleObj, matched, j,
			handlerQueue = [],
			args = slice.call( arguments ),
			handlers = ( jQuery._data( this, "events" ) || {} )[ event.type ] || [],
			special = jQuery.event.special[ event.type ] || {};

		// Use the fix-ed jQuery.Event rather than the (read-only) native event
		args[0] = event;
		event.delegateTarget = this;

		// Call the preDispatch hook for the mapped type, and let it bail if desired
		if ( special.preDispatch && special.preDispatch.call( this, event ) === false ) {
			return;
		}

		// Determine handlers
		handlerQueue = jQuery.event.handlers.call( this, event, handlers );

		// Run delegates first; they may want to stop propagation beneath us
		i = 0;
		while ( (matched = handlerQueue[ i++ ]) && !event.isPropagationStopped() ) {
			event.currentTarget = matched.elem;

			j = 0;
			while ( (handleObj = matched.handlers[ j++ ]) && !event.isImmediatePropagationStopped() ) {

				// Triggered event must either 1) have no namespace, or
				// 2) have namespace(s) a subset or equal to those in the bound event (both can have no namespace).
				if ( !event.namespace_re || event.namespace_re.test( handleObj.namespace ) ) {

					event.handleObj = handleObj;
					event.data = handleObj.data;

					ret = ( (jQuery.event.special[ handleObj.origType ] || {}).handle || handleObj.handler )
							.apply( matched.elem, args );

					if ( ret !== undefined ) {
						if ( (event.result = ret) === false ) {
							event.preventDefault();
							event.stopPropagation();
						}
					}
				}
			}
		}

		// Call the postDispatch hook for the mapped type
		if ( special.postDispatch ) {
			special.postDispatch.call( this, event );
		}

		return event.result;
	},

	handlers: function( event, handlers ) {
		var sel, handleObj, matches, i,
			handlerQueue = [],
			delegateCount = handlers.delegateCount,
			cur = event.target;

		// Find delegate handlers
		// Black-hole SVG <use> instance trees (#13180)
		// Avoid non-left-click bubbling in Firefox (#3861)
		if ( delegateCount && cur.nodeType && (!event.button || event.type !== "click") ) {

			/* jshint eqeqeq: false */
			for ( ; cur != this; cur = cur.parentNode || this ) {
				/* jshint eqeqeq: true */

				// Don't check non-elements (#13208)
				// Don't process clicks on disabled elements (#6911, #8165, #11382, #11764)
				if ( cur.nodeType === 1 && (cur.disabled !== true || event.type !== "click") ) {
					matches = [];
					for ( i = 0; i < delegateCount; i++ ) {
						handleObj = handlers[ i ];

						// Don't conflict with Object.prototype properties (#13203)
						sel = handleObj.selector + " ";

						if ( matches[ sel ] === undefined ) {
							matches[ sel ] = handleObj.needsContext ?
								jQuery( sel, this ).index( cur ) >= 0 :
								jQuery.find( sel, this, null, [ cur ] ).length;
						}
						if ( matches[ sel ] ) {
							matches.push( handleObj );
						}
					}
					if ( matches.length ) {
						handlerQueue.push({ elem: cur, handlers: matches });
					}
				}
			}
		}

		// Add the remaining (directly-bound) handlers
		if ( delegateCount < handlers.length ) {
			handlerQueue.push({ elem: this, handlers: handlers.slice( delegateCount ) });
		}

		return handlerQueue;
	},

	fix: function( event ) {
		if ( event[ jQuery.expando ] ) {
			return event;
		}

		// Create a writable copy of the event object and normalize some properties
		var i, prop, copy,
			type = event.type,
			originalEvent = event,
			fixHook = this.fixHooks[ type ];

		if ( !fixHook ) {
			this.fixHooks[ type ] = fixHook =
				rmouseEvent.test( type ) ? this.mouseHooks :
				rkeyEvent.test( type ) ? this.keyHooks :
				{};
		}
		copy = fixHook.props ? this.props.concat( fixHook.props ) : this.props;

		event = new jQuery.Event( originalEvent );

		i = copy.length;
		while ( i-- ) {
			prop = copy[ i ];
			event[ prop ] = originalEvent[ prop ];
		}

		// Support: IE<9
		// Fix target property (#1925)
		if ( !event.target ) {
			event.target = originalEvent.srcElement || document;
		}

		// Support: Chrome 23+, Safari?
		// Target should not be a text node (#504, #13143)
		if ( event.target.nodeType === 3 ) {
			event.target = event.target.parentNode;
		}

		// Support: IE<9
		// For mouse/key events, metaKey==false if it's undefined (#3368, #11328)
		event.metaKey = !!event.metaKey;

		return fixHook.filter ? fixHook.filter( event, originalEvent ) : event;
	},

	// Includes some event props shared by KeyEvent and MouseEvent
	props: "altKey bubbles cancelable ctrlKey currentTarget eventPhase metaKey relatedTarget shiftKey target timeStamp view which".split(" "),

	fixHooks: {},

	keyHooks: {
		props: "char charCode key keyCode".split(" "),
		filter: function( event, original ) {

			// Add which for key events
			if ( event.which == null ) {
				event.which = original.charCode != null ? original.charCode : original.keyCode;
			}

			return event;
		}
	},

	mouseHooks: {
		props: "button buttons clientX clientY fromElement offsetX offsetY pageX pageY screenX screenY toElement".split(" "),
		filter: function( event, original ) {
			var body, eventDoc, doc,
				button = original.button,
				fromElement = original.fromElement;

			// Calculate pageX/Y if missing and clientX/Y available
			if ( event.pageX == null && original.clientX != null ) {
				eventDoc = event.target.ownerDocument || document;
				doc = eventDoc.documentElement;
				body = eventDoc.body;

				event.pageX = original.clientX + ( doc && doc.scrollLeft || body && body.scrollLeft || 0 ) - ( doc && doc.clientLeft || body && body.clientLeft || 0 );
				event.pageY = original.clientY + ( doc && doc.scrollTop  || body && body.scrollTop  || 0 ) - ( doc && doc.clientTop  || body && body.clientTop  || 0 );
			}

			// Add relatedTarget, if necessary
			if ( !event.relatedTarget && fromElement ) {
				event.relatedTarget = fromElement === event.target ? original.toElement : fromElement;
			}

			// Add which for click: 1 === left; 2 === middle; 3 === right
			// Note: button is not normalized, so don't use it
			if ( !event.which && button !== undefined ) {
				event.which = ( button & 1 ? 1 : ( button & 2 ? 3 : ( button & 4 ? 2 : 0 ) ) );
			}

			return event;
		}
	},

	special: {
		load: {
			// Prevent triggered image.load events from bubbling to window.load
			noBubble: true
		},
		focus: {
			// Fire native event if possible so blur/focus sequence is correct
			trigger: function() {
				if ( this !== safeActiveElement() && this.focus ) {
					try {
						this.focus();
						return false;
					} catch ( e ) {
						// Support: IE<9
						// If we error on focus to hidden element (#1486, #12518),
						// let .trigger() run the handlers
					}
				}
			},
			delegateType: "focusin"
		},
		blur: {
			trigger: function() {
				if ( this === safeActiveElement() && this.blur ) {
					this.blur();
					return false;
				}
			},
			delegateType: "focusout"
		},
		click: {
			// For checkbox, fire native event so checked state will be right
			trigger: function() {
				if ( jQuery.nodeName( this, "input" ) && this.type === "checkbox" && this.click ) {
					this.click();
					return false;
				}
			},

			// For cross-browser consistency, don't fire native .click() on links
			_default: function( event ) {
				return jQuery.nodeName( event.target, "a" );
			}
		},

		beforeunload: {
			postDispatch: function( event ) {

				// Even when returnValue equals to undefined Firefox will still show alert
				if ( event.result !== undefined ) {
					event.originalEvent.returnValue = event.result;
				}
			}
		}
	},

	simulate: function( type, elem, event, bubble ) {
		// Piggyback on a donor event to simulate a different one.
		// Fake originalEvent to avoid donor's stopPropagation, but if the
		// simulated event prevents default then we do the same on the donor.
		var e = jQuery.extend(
			new jQuery.Event(),
			event,
			{
				type: type,
				isSimulated: true,
				originalEvent: {}
			}
		);
		if ( bubble ) {
			jQuery.event.trigger( e, null, elem );
		} else {
			jQuery.event.dispatch.call( elem, e );
		}
		if ( e.isDefaultPrevented() ) {
			event.preventDefault();
		}
	}
};

jQuery.removeEvent = document.removeEventListener ?
	function( elem, type, handle ) {
		if ( elem.removeEventListener ) {
			elem.removeEventListener( type, handle, false );
		}
	} :
	function( elem, type, handle ) {
		var name = "on" + type;

		if ( elem.detachEvent ) {

			// #8545, #7054, preventing memory leaks for custom events in IE6-8
			// detachEvent needed property on element, by name of that event, to properly expose it to GC
			if ( typeof elem[ name ] === strundefined ) {
				elem[ name ] = null;
			}

			elem.detachEvent( name, handle );
		}
	};

jQuery.Event = function( src, props ) {
	// Allow instantiation without the 'new' keyword
	if ( !(this instanceof jQuery.Event) ) {
		return new jQuery.Event( src, props );
	}

	// Event object
	if ( src && src.type ) {
		this.originalEvent = src;
		this.type = src.type;

		// Events bubbling up the document may have been marked as prevented
		// by a handler lower down the tree; reflect the correct value.
		this.isDefaultPrevented = src.defaultPrevented ||
				src.defaultPrevented === undefined && (
				// Support: IE < 9
				src.returnValue === false ||
				// Support: Android < 4.0
				src.getPreventDefault && src.getPreventDefault() ) ?
			returnTrue :
			returnFalse;

	// Event type
	} else {
		this.type = src;
	}

	// Put explicitly provided properties onto the event object
	if ( props ) {
		jQuery.extend( this, props );
	}

	// Create a timestamp if incoming event doesn't have one
	this.timeStamp = src && src.timeStamp || jQuery.now();

	// Mark it as fixed
	this[ jQuery.expando ] = true;
};

// jQuery.Event is based on DOM3 Events as specified by the ECMAScript Language Binding
// http://www.w3.org/TR/2003/WD-DOM-Level-3-Events-20030331/ecma-script-binding.html
jQuery.Event.prototype = {
	isDefaultPrevented: returnFalse,
	isPropagationStopped: returnFalse,
	isImmediatePropagationStopped: returnFalse,

	preventDefault: function() {
		var e = this.originalEvent;

		this.isDefaultPrevented = returnTrue;
		if ( !e ) {
			return;
		}

		// If preventDefault exists, run it on the original event
		if ( e.preventDefault ) {
			e.preventDefault();

		// Support: IE
		// Otherwise set the returnValue property of the original event to false
		} else {
			e.returnValue = false;
		}
	},
	stopPropagation: function() {
		var e = this.originalEvent;

		this.isPropagationStopped = returnTrue;
		if ( !e ) {
			return;
		}
		// If stopPropagation exists, run it on the original event
		if ( e.stopPropagation ) {
			e.stopPropagation();
		}

		// Support: IE
		// Set the cancelBubble property of the original event to true
		e.cancelBubble = true;
	},
	stopImmediatePropagation: function() {
		this.isImmediatePropagationStopped = returnTrue;
		this.stopPropagation();
	}
};

// Create mouseenter/leave events using mouseover/out and event-time checks
jQuery.each({
	pointerenter: "pointerover",  // BHTEST
	pointerleave: "pointerout",    // BHTEST
	mouseenter: "mouseover",
	mouseleave: "mouseout"
}, function( orig, fix ) {
	jQuery.event.special[ orig ] = {
		delegateType: fix,
		bindType: fix,

		handle: function( event ) {
			var ret,
				target = this,
				related = event.relatedTarget,
				handleObj = event.handleObj;

			// For mousenter/leave call the handler if related is outside the target.
			// NB: No relatedTarget if the mouse left/entered the browser window
			if ( !related || (related !== target && !jQuery.contains( target, related )) ) {
				event.type = handleObj.origType;
				ret = handleObj.handler.apply( this, arguments );
				event.type = fix;
			}
			return ret;
		}
	};
});

// IE submit delegation
if ( !support.submitBubbles ) {

	jQuery.event.special.submit = {
		setup: function() {
			// Only need this for delegated form submit events
			if ( jQuery.nodeName( this, "form" ) ) {
				return false;
			}

			// Lazy-add a submit handler when a descendant form may potentially be submitted
			jQuery.event.add( this, "click._submit keypress._submit", function( e ) {
				// Node name check avoids a VML-related crash in IE (#9807)
				var elem = e.target,
					form = jQuery.nodeName( elem, "input" ) || jQuery.nodeName( elem, "button" ) ? elem.form : undefined;
				if ( form && !jQuery._data( form, "submitBubbles" ) ) {
					jQuery.event.add( form, "submit._submit", function( event ) {
						event._submit_bubble = true;
					});
					jQuery._data( form, "submitBubbles", true );
				}
			});
			// return undefined since we don't need an event listener
		},

		postDispatch: function( event ) {
			// If form was submitted by the user, bubble the event up the tree
			if ( event._submit_bubble ) {
				delete event._submit_bubble;
				if ( this.parentNode && !event.isTrigger ) {
					jQuery.event.simulate( "submit", this.parentNode, event, true );
				}
			}
		},

		teardown: function() {
			// Only need this for delegated form submit events
			if ( jQuery.nodeName( this, "form" ) ) {
				return false;
			}

			// Remove delegated handlers; cleanData eventually reaps submit handlers attached above
			jQuery.event.remove( this, "._submit" );
		}
	};
}

// IE change delegation and checkbox/radio fix
if ( !support.changeBubbles ) {

	jQuery.event.special.change = {

		setup: function() {

			if ( rformElems.test( this.nodeName ) ) {
				// IE doesn't fire change on a check/radio until blur; trigger it on click
				// after a propertychange. Eat the blur-change in special.change.handle.
				// This still fires onchange a second time for check/radio after blur.
				if ( this.type === "checkbox" || this.type === "radio" ) {
					jQuery.event.add( this, "propertychange._change", function( event ) {
						if ( event.originalEvent.propertyName === "checked" ) {
							this._just_changed = true;
						}
					});
					jQuery.event.add( this, "click._change", function( event ) {
						if ( this._just_changed && !event.isTrigger ) {
							this._just_changed = false;
						}
						// Allow triggered, simulated change events (#11500)
						jQuery.event.simulate( "change", this, event, true );
					});
				}
				return false;
			}
			// Delegated event; lazy-add a change handler on descendant inputs
			jQuery.event.add( this, "beforeactivate._change", function( e ) {
				var elem = e.target;

				if ( rformElems.test( elem.nodeName ) && !jQuery._data( elem, "changeBubbles" ) ) {
					jQuery.event.add( elem, "change._change", function( event ) {
						if ( this.parentNode && !event.isSimulated && !event.isTrigger ) {
							jQuery.event.simulate( "change", this.parentNode, event, true );
						}
					});
					jQuery._data( elem, "changeBubbles", true );
				}
			});
		},

		handle: function( event ) {
			var elem = event.target;

			// Swallow native change events from checkbox/radio, we already triggered them above
			if ( this !== elem || event.isSimulated || event.isTrigger || (elem.type !== "radio" && elem.type !== "checkbox") ) {
				return event.handleObj.handler.apply( this, arguments );
			}
		},

		teardown: function() {
			jQuery.event.remove( this, "._change" );

			return !rformElems.test( this.nodeName );
		}
	};
}

// Create "bubbling" focus and blur events
if ( !support.focusinBubbles ) {
	jQuery.each({ focus: "focusin", blur: "focusout" }, function( orig, fix ) {

		// Attach a single capturing handler on the document while someone wants focusin/focusout
		var handler = function( event ) {
				jQuery.event.simulate( fix, event.target, jQuery.event.fix( event ), true );
			};

		jQuery.event.special[ fix ] = {
			setup: function() {
				var doc = this.ownerDocument || this,
					attaches = jQuery._data( doc, fix );

				if ( !attaches ) {
					doc.addEventListener( orig, handler, true );
				}
				jQuery._data( doc, fix, ( attaches || 0 ) + 1 );
			},
			teardown: function() {
				var doc = this.ownerDocument || this,
					attaches = jQuery._data( doc, fix ) - 1;

				if ( !attaches ) {
					doc.removeEventListener( orig, handler, true );
					jQuery._removeData( doc, fix );
				} else {
					jQuery._data( doc, fix, attaches );
				}
			}
		};
	});
}

jQuery.fn.extend({

	on: function( types, selector, data, fn, /*INTERNAL*/ one ) {
		var type, origFn;

		// Types can be a map of types/handlers
		if ( typeof types === "object" ) {
			// ( types-Object, selector, data )
			if ( typeof selector !== "string" ) {
				// ( types-Object, data )
				data = data || selector;
				selector = undefined;
			}
			for ( type in types ) {
				this.on( type, selector, data, types[ type ], one );
			}
			return this;
		}

		if ( data == null && fn == null ) {
			// ( types, fn )
			fn = selector;
			data = selector = undefined;
		} else if ( fn == null ) {
			if ( typeof selector === "string" ) {
				// ( types, selector, fn )
				fn = data;
				data = undefined;
			} else {
				// ( types, data, fn )
				fn = data;
				data = selector;
				selector = undefined;
			}
		}
		if ( fn === false ) {
			fn = returnFalse;
		} else if ( !fn ) {
			return this;
		}

		if ( one === 1 ) {
			origFn = fn;
			fn = function( event ) {
				// Can use an empty set, since event contains the info
				jQuery().off( event );
				return origFn.apply( this, arguments );
			};
			// Use same guid so caller can remove using origFn
			fn.guid = origFn.guid || ( origFn.guid = jQuery.guid++ );
		}
		return this.each( function() {
			jQuery.event.add( this, types, fn, data, selector );
		});
	},
	one: function( types, selector, data, fn ) {
		return this.on( types, selector, data, fn, 1 );
	},
	off: function( types, selector, fn ) {
		var handleObj, type;
		if ( types && types.preventDefault && types.handleObj ) {
			// ( event )  dispatched jQuery.Event
			handleObj = types.handleObj;
			jQuery( types.delegateTarget ).off(
				handleObj.namespace ? handleObj.origType + "." + handleObj.namespace : handleObj.origType,
				handleObj.selector,
				handleObj.handler
			);
			return this;
		}
		if ( typeof types === "object" ) {
			// ( types-object [, selector] )
			for ( type in types ) {
				this.off( type, selector, types[ type ] );
			}
			return this;
		}
		if ( selector === false || typeof selector === "function" ) {
			// ( types [, fn] )
			fn = selector;
			selector = undefined;
		}
		if ( fn === false ) {
			fn = returnFalse;
		}
		return this.each(function() {
			jQuery.event.remove( this, types, fn, selector );
		});
	},

	trigger: function( type, data ) {
		return this.each(function() {
			jQuery.event.trigger( type, data, this );
		});
	},
	triggerHandler: function( type, data ) {
		var elem = this[0];
		if ( elem ) {
			return jQuery.event.trigger( type, data, elem, true );
		}
	}
});


function createSafeFragment( document ) {
	var list = nodeNames.split( "|" ),
		safeFrag = document.createDocumentFragment();

	if ( safeFrag.createElement ) {
		while ( list.length ) {
			safeFrag.createElement(
				list.pop()
			);
		}
	}
	return safeFrag;
}

var nodeNames = "abbr|article|aside|audio|bdi|canvas|data|datalist|details|figcaption|figure|footer|" +
		"header|hgroup|mark|meter|nav|output|progress|section|summary|time|video",
	rinlinejQuery = / jQuery\d+="(?:null|\d+)"/g,
	rnoshimcache = new RegExp("<(?:" + nodeNames + ")[\\s/>]", "i"),
	rleadingWhitespace = /^\s+/,
	rxhtmlTag = /<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/gi,
	rtagName = /<([\w:]+)/,
	rtbody = /<tbody/i,
	rhtml = /<|&#?\w+;/,
	rnoInnerhtml = /<(?:script|style|link)/i,
	// checked="checked" or checked
	rchecked = /checked\s*(?:[^=]|=\s*.checked.)/i,
	rscriptType = /^$|\/(?:java|ecma)script/i,
	rscriptTypeMasked = /^true\/(.*)/,
	rcleanScript = /^\s*<!(?:\[CDATA\[|--)|(?:\]\]|--)>\s*$/g,

	// We have to close these tags to support XHTML (#13200)
	wrapMap = {
		option: [ 1, "<select multiple='multiple'>", "</select>" ],
		legend: [ 1, "<fieldset>", "</fieldset>" ],
		area: [ 1, "<map>", "</map>" ],
		param: [ 1, "<object>", "</object>" ],
		thead: [ 1, "<table>", "</table>" ],
		tr: [ 2, "<table><tbody>", "</tbody></table>" ],
		col: [ 2, "<table><tbody></tbody><colgroup>", "</colgroup></table>" ],
		td: [ 3, "<table><tbody><tr>", "</tr></tbody></table>" ],

		// IE6-8 can't serialize link, script, style, or any html5 (NoScope) tags,
		// unless wrapped in a div with non-breaking characters in front of it.
		_default: support.htmlSerialize ? [ 0, "", "" ] : [ 1, "X<div>", "</div>"  ]
	},
	safeFragment = createSafeFragment( document ),
	fragmentDiv = safeFragment.appendChild( document.createElement("div") );

wrapMap.optgroup = wrapMap.option;
wrapMap.tbody = wrapMap.tfoot = wrapMap.colgroup = wrapMap.caption = wrapMap.thead;
wrapMap.th = wrapMap.td;

function getAll( context, tag ) {
	var elems, elem,
		i = 0,
		found = typeof context.getElementsByTagName !== strundefined ? context.getElementsByTagName( tag || "*" ) :
			typeof context.querySelectorAll !== strundefined ? context.querySelectorAll( tag || "*" ) :
			undefined;

	if ( !found ) {
		for ( found = [], elems = context.childNodes || context; (elem = elems[i]) != null; i++ ) {
			if ( !tag || jQuery.nodeName( elem, tag ) ) {
				found.push( elem );
			} else {
				jQuery.merge( found, getAll( elem, tag ) );
			}
		}
	}

	return tag === undefined || tag && jQuery.nodeName( context, tag ) ?
		jQuery.merge( [ context ], found ) :
		found;
}

// Used in buildFragment, fixes the defaultChecked property
function fixDefaultChecked( elem ) {
	if ( rcheckableType.test( elem.type ) ) {
		elem.defaultChecked = elem.checked;
	}
}

// Support: IE<8
// Manipulating tables requires a tbody
function manipulationTarget( elem, content ) {
	return jQuery.nodeName( elem, "table" ) &&
		jQuery.nodeName( content.nodeType !== 11 ? content : content.firstChild, "tr" ) ?

		elem.getElementsByTagName("tbody")[0] ||
			elem.appendChild( elem.ownerDocument.createElement("tbody") ) :
		elem;
}

// Replace/restore the type attribute of script elements for safe DOM manipulation
function disableScript( elem ) {
	elem.type = (jQuery.find.attr( elem, "type" ) !== null) + "/" + elem.type;
	return elem;
}
function restoreScript( elem ) {
	var match = rscriptTypeMasked.exec( elem.type );
	if ( match ) {
		elem.type = match[1];
	} else {
		elem.removeAttribute("type");
	}
	return elem;
}

// Mark scripts as having already been evaluated
function setGlobalEval( elems, refElements ) {
	var elem,
		i = 0;
	for ( ; (elem = elems[i]) != null; i++ ) {
		jQuery._data( elem, "globalEval", !refElements || jQuery._data( refElements[i], "globalEval" ) );
	}
}

function cloneCopyEvent( src, dest ) {

	if ( dest.nodeType !== 1 || !jQuery.hasData( src ) ) {
		return;
	}

	var type, i, l,
		oldData = jQuery._data( src ),
		curData = jQuery._data( dest, oldData ),
		events = oldData.events;

	if ( events ) {
		delete curData.handle;
		curData.events = {};

		for ( type in events ) {
			for ( i = 0, l = events[ type ].length; i < l; i++ ) {
				jQuery.event.add( dest, type, events[ type ][ i ] );
			}
		}
	}

	// make the cloned public data object a copy from the original
	if ( curData.data ) {
		curData.data = jQuery.extend( {}, curData.data );
	}
}

function fixCloneNodeIssues( src, dest ) {
	var nodeName, e, data;

	// We do not need to do anything for non-Elements
	if ( dest.nodeType !== 1 ) {
		return;
	}

	nodeName = dest.nodeName.toLowerCase();

	// IE6-8 copies events bound via attachEvent when using cloneNode.
	if ( !support.noCloneEvent && dest[ jQuery.expando ] ) {
		data = jQuery._data( dest );

		for ( e in data.events ) {
			jQuery.removeEvent( dest, e, data.handle );
		}

		// Event data gets referenced instead of copied if the expando gets copied too
		dest.removeAttribute( jQuery.expando );
	}

	// IE blanks contents when cloning scripts, and tries to evaluate newly-set text
	if ( nodeName === "script" && dest.text !== src.text ) {
		disableScript( dest ).text = src.text;
		restoreScript( dest );

	// IE6-10 improperly clones children of object elements using classid.
	// IE10 throws NoModificationAllowedError if parent is null, #12132.
	} else if ( nodeName === "object" ) {
		if ( dest.parentNode ) {
			dest.outerHTML = src.outerHTML;
		}

		// This path appears unavoidable for IE9. When cloning an object
		// element in IE9, the outerHTML strategy above is not sufficient.
		// If the src has innerHTML and the destination does not,
		// copy the src.innerHTML into the dest.innerHTML. #10324
		if ( support.html5Clone && ( src.innerHTML && !jQuery.trim(dest.innerHTML) ) ) {
			dest.innerHTML = src.innerHTML;
		}

	} else if ( nodeName === "input" && rcheckableType.test( src.type ) ) {
		// IE6-8 fails to persist the checked state of a cloned checkbox
		// or radio button. Worse, IE6-7 fail to give the cloned element
		// a checked appearance if the defaultChecked value isn't also set

		dest.defaultChecked = dest.checked = src.checked;

		// IE6-7 get confused and end up setting the value of a cloned
		// checkbox/radio button to an empty string instead of "on"
		if ( dest.value !== src.value ) {
			dest.value = src.value;
		}

	// IE6-8 fails to return the selected option to the default selected
	// state when cloning options
	} else if ( nodeName === "option" ) {
		dest.defaultSelected = dest.selected = src.defaultSelected;

	// IE6-8 fails to set the defaultValue to the correct value when
	// cloning other types of input fields
	} else if ( nodeName === "input" || nodeName === "textarea" ) {
		dest.defaultValue = src.defaultValue;
	}
}

jQuery.extend({
	clone: function( elem, dataAndEvents, deepDataAndEvents ) {
		var destElements, node, clone, i, srcElements,
			inPage = jQuery.contains( elem.ownerDocument, elem );

		if ( support.html5Clone || jQuery.isXMLDoc(elem) || !rnoshimcache.test( "<" + elem.nodeName + ">" ) ) {
			clone = elem.cloneNode( true );

		// IE<=8 does not properly clone detached, unknown element nodes
		} else {
			fragmentDiv.innerHTML = elem.outerHTML;
			fragmentDiv.removeChild( clone = fragmentDiv.firstChild );
		}

		if ( (!support.noCloneEvent || !support.noCloneChecked) &&
				(elem.nodeType === 1 || elem.nodeType === 11) && !jQuery.isXMLDoc(elem) ) {

			// We eschew Sizzle here for performance reasons: http://jsperf.com/getall-vs-sizzle/2
			destElements = getAll( clone );
			srcElements = getAll( elem );

			// Fix all IE cloning issues
			for ( i = 0; (node = srcElements[i]) != null; ++i ) {
				// Ensure that the destination node is not null; Fixes #9587
				if ( destElements[i] ) {
					fixCloneNodeIssues( node, destElements[i] );
				}
			}
		}

		// Copy the events from the original to the clone
		if ( dataAndEvents ) {
			if ( deepDataAndEvents ) {
				srcElements = srcElements || getAll( elem );
				destElements = destElements || getAll( clone );

				for ( i = 0; (node = srcElements[i]) != null; i++ ) {
					cloneCopyEvent( node, destElements[i] );
				}
			} else {
				cloneCopyEvent( elem, clone );
			}
		}

		// Preserve script evaluation history
		destElements = getAll( clone, "script" );
		if ( destElements.length > 0 ) {
			setGlobalEval( destElements, !inPage && getAll( elem, "script" ) );
		}

		destElements = srcElements = node = null;

		// Return the cloned set
		return clone;
	},

	buildFragment: function( elems, context, scripts, selection ) {
		var j, elem, contains,
			tmp, tag, tbody, wrap,
			l = elems.length,

			// Ensure a safe fragment
			safe = createSafeFragment( context ),

			nodes = [],
			i = 0;

		for ( ; i < l; i++ ) {
			elem = elems[ i ];

			if ( elem || elem === 0 ) {

				// Add nodes directly
				if ( jQuery.type( elem ) === "object" ) {
					jQuery.merge( nodes, elem.nodeType ? [ elem ] : elem );

				// Convert non-html into a text node
				} else if ( !rhtml.test( elem ) ) {
					nodes.push( context.createTextNode( elem ) );

				// Convert html into DOM nodes
				} else {
					tmp = tmp || safe.appendChild( context.createElement("div") );

					// Deserialize a standard representation
					tag = (rtagName.exec( elem ) || [ "", "" ])[ 1 ].toLowerCase();
					wrap = wrapMap[ tag ] || wrapMap._default;

					tmp.innerHTML = wrap[1] + elem.replace( rxhtmlTag, "<$1></$2>" ) + wrap[2];

					// Descend through wrappers to the right content
					j = wrap[0];
					while ( j-- ) {
						tmp = tmp.lastChild;
					}

					// Manually add leading whitespace removed by IE
					if ( !support.leadingWhitespace && rleadingWhitespace.test( elem ) ) {
						nodes.push( context.createTextNode( rleadingWhitespace.exec( elem )[0] ) );
					}

					// Remove IE's autoinserted <tbody> from table fragments
					if ( !support.tbody ) {

						// String was a <table>, *may* have spurious <tbody>
						elem = tag === "table" && !rtbody.test( elem ) ?
							tmp.firstChild :

							// String was a bare <thead> or <tfoot>
							wrap[1] === "<table>" && !rtbody.test( elem ) ?
								tmp :
								0;

						j = elem && elem.childNodes.length;
						while ( j-- ) {
							if ( jQuery.nodeName( (tbody = elem.childNodes[j]), "tbody" ) && !tbody.childNodes.length ) {
								elem.removeChild( tbody );
							}
						}
					}

					jQuery.merge( nodes, tmp.childNodes );

					// Fix #12392 for WebKit and IE > 9
					tmp.textContent = "";

					// Fix #12392 for oldIE
					while ( tmp.firstChild ) {
						tmp.removeChild( tmp.firstChild );
					}

					// Remember the top-level container for proper cleanup
					tmp = safe.lastChild;
				}
			}
		}

		// Fix #11356: Clear elements from fragment
		if ( tmp ) {
			safe.removeChild( tmp );
		}

		// Reset defaultChecked for any radios and checkboxes
		// about to be appended to the DOM in IE 6/7 (#8060)
		if ( !support.appendChecked ) {
			jQuery.grep( getAll( nodes, "input" ), fixDefaultChecked );
		}

		i = 0;
		while ( (elem = nodes[ i++ ]) ) {

			// #4087 - If origin and destination elements are the same, and this is
			// that element, do not do anything
			if ( selection && jQuery.inArray( elem, selection ) !== -1 ) {
				continue;
			}

			contains = jQuery.contains( elem.ownerDocument, elem );

			// Append to fragment
			tmp = getAll( safe.appendChild( elem ), "script" );

			// Preserve script evaluation history
			if ( contains ) {
				setGlobalEval( tmp );
			}

			// Capture executables
			if ( scripts ) {
				j = 0;
				while ( (elem = tmp[ j++ ]) ) {
					if ( rscriptType.test( elem.type || "" ) ) {
						scripts.push( elem );
					}
				}
			}
		}

		tmp = null;

		return safe;
	},

	cleanData: function( elems, /* internal */ acceptData ) {
		var elem, type, id, data,
			i = 0,
			internalKey = jQuery.expando,
			cache = jQuery.cache,
			deleteExpando = support.deleteExpando,
			special = jQuery.event.special;

		for ( ; (elem = elems[i]) != null; i++ ) {
			if ( acceptData || jQuery.acceptData( elem ) ) {

				id = elem[ internalKey ];
				data = id && cache[ id ];

				if ( data ) {
					if ( data.events ) {
						for ( type in data.events ) {
							if ( special[ type ] ) {
								jQuery.event.remove( elem, type );

							// This is a shortcut to avoid jQuery.event.remove's overhead
							} else {
								jQuery.removeEvent( elem, type, data.handle );
							}
						}
					}

					// Remove cache only if it was not already removed by jQuery.event.remove
					if ( cache[ id ] ) {

						delete cache[ id ];

						// IE does not allow us to delete expando properties from nodes,
						// nor does it have a removeAttribute function on Document nodes;
						// we must handle all of these cases
						if ( deleteExpando ) {
							delete elem[ internalKey ];

						} else if ( typeof elem.removeAttribute !== strundefined ) {
							elem.removeAttribute( internalKey );

						} else {
							elem[ internalKey ] = null;
						}

						deletedIds.push( id );
					}
				}
			}
		}
	}
});

jQuery.fn.extend({
	text: function( value ) {
		return access( this, function( value ) {
			return value === undefined ?
				jQuery.text( this ) :
				this.empty().append( ( this[0] && this[0].ownerDocument || document ).createTextNode( value ) );
		}, null, value, arguments.length );
	},

	append: function() {
		return this.domManip( arguments, function( elem ) {
			if ( this.nodeType === 1 || this.nodeType === 11 || this.nodeType === 9 ) {
				var target = manipulationTarget( this, elem );
				target.appendChild( elem );
			}
		});
	},

	prepend: function() {
		return this.domManip( arguments, function( elem ) {
			if ( this.nodeType === 1 || this.nodeType === 11 || this.nodeType === 9 ) {
				var target = manipulationTarget( this, elem );
				target.insertBefore( elem, target.firstChild );
			}
		});
	},

	before: function() {
		return this.domManip( arguments, function( elem ) {
			if ( this.parentNode ) {
				this.parentNode.insertBefore( elem, this );
			}
		});
	},

	after: function() {
		return this.domManip( arguments, function( elem ) {
			if ( this.parentNode ) {
				this.parentNode.insertBefore( elem, this.nextSibling );
			}
		});
	},

	remove: function( selector, keepData /* Internal Use Only */ ) {
		var elem,
			elems = selector ? jQuery.filter( selector, this ) : this,
			i = 0;

		for ( ; (elem = elems[i]) != null; i++ ) {

			if ( !keepData && elem.nodeType === 1 ) {
				jQuery.cleanData( getAll( elem ) );
			}

			if ( elem.parentNode ) {
				if ( keepData && jQuery.contains( elem.ownerDocument, elem ) ) {
					setGlobalEval( getAll( elem, "script" ) );
				}
				elem.parentNode.removeChild( elem );
			}
		}

		return this;
	},

	empty: function() {
		var elem,
			i = 0;

		for ( ; (elem = this[i]) != null; i++ ) {
			// Remove element nodes and prevent memory leaks
			if ( elem.nodeType === 1 ) {
				jQuery.cleanData( getAll( elem, false ) );
			}

			// Remove any remaining nodes
			while ( elem.firstChild ) {
				elem.removeChild( elem.firstChild );
			}

			// If this is a select, ensure that it displays empty (#12336)
			// Support: IE<9
			if ( elem.options && jQuery.nodeName( elem, "select" ) ) {
				elem.options.length = 0;
			}
		}

		return this;
	},

	clone: function( dataAndEvents, deepDataAndEvents ) {
		dataAndEvents = dataAndEvents == null ? false : dataAndEvents;
		deepDataAndEvents = deepDataAndEvents == null ? dataAndEvents : deepDataAndEvents;

		return this.map(function() {
			return jQuery.clone( this, dataAndEvents, deepDataAndEvents );
		});
	},

	html: function( value ) {
		return access( this, function( value ) {
			var elem = this[ 0 ] || {},
				i = 0,
				l = this.length;

			if ( value === undefined ) {
				return elem.nodeType === 1 ?
					elem.innerHTML.replace( rinlinejQuery, "" ) :
					undefined;
			}

			// See if we can take a shortcut and just use innerHTML
			if ( typeof value === "string" && !rnoInnerhtml.test( value ) &&
				( support.htmlSerialize || !rnoshimcache.test( value )  ) &&
				( support.leadingWhitespace || !rleadingWhitespace.test( value ) ) &&
				!wrapMap[ (rtagName.exec( value ) || [ "", "" ])[ 1 ].toLowerCase() ] ) {

				value = value.replace( rxhtmlTag, "<$1></$2>" );

				try {
					for (; i < l; i++ ) {
						// Remove element nodes and prevent memory leaks
						elem = this[i] || {};
						if ( elem.nodeType === 1 ) {
							jQuery.cleanData( getAll( elem, false ) );
							elem.innerHTML = value;
						}
					}

					elem = 0;

				// If using innerHTML throws an exception, use the fallback method
				} catch(e) {}
			}

			if ( elem ) {
				this.empty().append( value );
			}
		}, null, value, arguments.length );
	},

	replaceWith: function() {
		var arg = arguments[ 0 ];

		// Make the changes, replacing each context element with the new content
		this.domManip( arguments, function( elem ) {
			arg = this.parentNode;

			jQuery.cleanData( getAll( this ) );

			if ( arg ) {
				arg.replaceChild( elem, this );
			}
		});

		// Force removal if there was no new content (e.g., from empty arguments)
		return arg && (arg.length || arg.nodeType) ? this : this.remove();
	},

	detach: function( selector ) {
		return this.remove( selector, true );
	},

	domManip: function( args, callback ) {

		// Flatten any nested arrays
		args = concat.apply( [], args );

		var first, node, hasScripts,
			scripts, doc, fragment,
			i = 0,
			l = this.length,
			set = this,
			iNoClone = l - 1,
			value = args[0],
			isFunction = jQuery.isFunction( value );

		// We can't cloneNode fragments that contain checked, in WebKit
		if ( isFunction ||
				( l > 1 && typeof value === "string" &&
					!support.checkClone && rchecked.test( value ) ) ) {
			return this.each(function( index ) {
				var self = set.eq( index );
				if ( isFunction ) {
					args[0] = value.call( this, index, self.html() );
				}
				self.domManip( args, callback );
			});
		}

		if ( l ) {
			fragment = jQuery.buildFragment( args, this[ 0 ].ownerDocument, false, this );
			first = fragment.firstChild;

			if ( fragment.childNodes.length === 1 ) {
				fragment = first;
			}

			if ( first ) {
				scripts = jQuery.map( getAll( fragment, "script" ), disableScript );
				hasScripts = scripts.length;

				// Use the original fragment for the last item instead of the first because it can end up
				// being emptied incorrectly in certain situations (#8070).
				for ( ; i < l; i++ ) {
					node = fragment;

					if ( i !== iNoClone ) {
						node = jQuery.clone( node, true, true );

						// Keep references to cloned scripts for later restoration
						if ( hasScripts ) {
							jQuery.merge( scripts, getAll( node, "script" ) );
						}
					}

					callback.call( this[i], node, i );
				}

				if ( hasScripts ) {
					doc = scripts[ scripts.length - 1 ].ownerDocument;

					// Reenable scripts
					jQuery.map( scripts, restoreScript );

					// Evaluate executable scripts on first document insertion
					for ( i = 0; i < hasScripts; i++ ) {
						node = scripts[ i ];
						if ( rscriptType.test( node.type || "" ) &&
							!jQuery._data( node, "globalEval" ) && jQuery.contains( doc, node ) ) {

							if ( node.src ) {
								// Optional AJAX dependency, but won't run scripts if not present
								if ( jQuery._evalUrl ) {
									jQuery._evalUrl( node.src );
								}
							} else {
								jQuery.globalEval( ( node.text || node.textContent || node.innerHTML || "" ).replace( rcleanScript, "" ) );
							}
						}
					}
				}

				// Fix #11809: Avoid leaking memory
				fragment = first = null;
			}
		}

		return this;
	}
});

jQuery.each({
	appendTo: "append",
	prependTo: "prepend",
	insertBefore: "before",
	insertAfter: "after",
	replaceAll: "replaceWith"
}, function( name, original ) {
	jQuery.fn[ name ] = function( selector ) {
		var elems,
			i = 0,
			ret = [],
			insert = jQuery( selector ),
			last = insert.length - 1;

		for ( ; i <= last; i++ ) {
			elems = i === last ? this : this.clone(true);
			jQuery( insert[i] )[ original ]( elems );

			// Modern browsers can apply jQuery collections as arrays, but oldIE needs a .get()
			push.apply( ret, elems.get() );
		}

		return this.pushStack( ret );
	};
});


var iframe,
	elemdisplay = {};

/**
 * Retrieve the actual display of a element
 * @param {String} name nodeName of the element
 * @param {Object} doc Document object
 */
// Called only from within defaultDisplay
function actualDisplay( name, doc ) {
	var elem = jQuery( doc.createElement( name ) ).appendTo( doc.body ),

		// getDefaultComputedStyle might be reliably used only on attached element
		display = window.getDefaultComputedStyle ?

			// Use of this method is a temporary fix (more like optmization) until something better comes along,
			// since it was removed from specification and supported only in FF
			window.getDefaultComputedStyle( elem[ 0 ] ).display : jQuery.css( elem[ 0 ], "display" );

	// We don't have any data stored on the element,
	// so use "detach" method as fast way to get rid of the element
	elem.detach();

	return display;
}

/**
 * Try to determine the default display value of an element
 * @param {String} nodeName
 */
function defaultDisplay( nodeName ) {
	var doc = document,
		display = elemdisplay[ nodeName ];

	if ( !display ) {
		display = actualDisplay( nodeName, doc );

		// If the simple way fails, read from inside an iframe
		if ( display === "none" || !display ) {

			// Use the already-created iframe if possible
			iframe = (iframe || jQuery( "<iframe frameborder='0' width='0' height='0'/>" )).appendTo( doc.documentElement );

			// Always write a new HTML skeleton so Webkit and Firefox don't choke on reuse
			doc = ( iframe[ 0 ].contentWindow || iframe[ 0 ].contentDocument ).document;

			// Support: IE
			doc.write();
			doc.close();

			display = actualDisplay( nodeName, doc );
			iframe.detach();
		}

		// Store the correct default display
		elemdisplay[ nodeName ] = display;
	}

	return display;
}


(function() {
	var a, shrinkWrapBlocksVal,
		div = document.createElement( "div" ),
		divReset =
			"-webkit-box-sizing:content-box;-moz-box-sizing:content-box;box-sizing:content-box;" +
			"display:block;padding:0;margin:0;border:0";

	// Setup
	div.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>";
	a = div.getElementsByTagName( "a" )[ 0 ];

	a.style.cssText = "float:left;opacity:.5";

	// Make sure that element opacity exists
	// (IE uses filter instead)
	// Use a regex to work around a WebKit issue. See #5145
	support.opacity = /^0.5/.test( a.style.opacity );

	// Verify style float existence
	// (IE uses styleFloat instead of cssFloat)
	support.cssFloat = !!a.style.cssFloat;

	div.style.backgroundClip = "content-box";
	div.cloneNode( true ).style.backgroundClip = "";
	support.clearCloneStyle = div.style.backgroundClip === "content-box";

	// Null elements to avoid leaks in IE.
	a = div = null;

	support.shrinkWrapBlocks = function() {
		var body, container, div, containerStyles;

		if ( shrinkWrapBlocksVal == null ) {
			body = document.getElementsByTagName( "body" )[ 0 ];
			if ( !body ) {
				// Test fired too early or in an unsupported environment, exit.
				return;
			}

			containerStyles = "border:0;width:0;height:0;position:absolute;top:0;left:-9999px";
			container = document.createElement( "div" );
			div = document.createElement( "div" );

			body.appendChild( container ).appendChild( div );

			// Will be changed later if needed.
			shrinkWrapBlocksVal = false;

			if ( typeof div.style.zoom !== strundefined ) {
				// Support: IE6
				// Check if elements with layout shrink-wrap their children
				div.style.cssText = divReset + ";width:1px;padding:1px;zoom:1";
				div.innerHTML = "<div></div>";
				div.firstChild.style.width = "5px";
				shrinkWrapBlocksVal = div.offsetWidth !== 3;
			}

			body.removeChild( container );

			// Null elements to avoid leaks in IE.
			body = container = div = null;
		}

		return shrinkWrapBlocksVal;
	};

})();
var rmargin = (/^margin/);

var rnumnonpx = new RegExp( "^(" + pnum + ")(?!px)[a-z%]+$", "i" );



var getStyles, curCSS,
	rposition = /^(top|right|bottom|left)$/;

if ( window.getComputedStyle ) {
	getStyles = function( elem ) {
		return elem.ownerDocument.defaultView.getComputedStyle( elem, null );
	};

	curCSS = function( elem, name, computed ) {
		var width, minWidth, maxWidth, ret,
			style = elem.style;

		computed = computed || getStyles( elem );

		// getPropertyValue is only needed for .css('filter') in IE9, see #12537
		ret = computed ? computed.getPropertyValue( name ) || computed[ name ] : undefined;

		if ( computed ) {

			if ( ret === "" && !jQuery.contains( elem.ownerDocument, elem ) ) {
				ret = jQuery.style( elem, name );
			}

			// A tribute to the "awesome hack by Dean Edwards"
			// Chrome < 17 and Safari 5.0 uses "computed value" instead of "used value" for margin-right
			// Safari 5.1.7 (at least) returns percentage for a larger set of values, but width seems to be reliably pixels
			// this is against the CSSOM draft spec: http://dev.w3.org/csswg/cssom/#resolved-values
			if ( rnumnonpx.test( ret ) && rmargin.test( name ) ) {

				// Remember the original values
				width = style.width;
				minWidth = style.minWidth;
				maxWidth = style.maxWidth;

				// Put in the new values to get a computed value out
				style.minWidth = style.maxWidth = style.width = ret;
				ret = computed.width;

				// Revert the changed values
				style.width = width;
				style.minWidth = minWidth;
				style.maxWidth = maxWidth;
			}
		}

		// Support: IE
		// IE returns zIndex value as an integer.
		return ret === undefined ?
			ret :
			ret + "";
	};
} else if ( document.documentElement.currentStyle ) {
	getStyles = function( elem ) {
		return elem.currentStyle;
	};

	curCSS = function( elem, name, computed ) {
		var left, rs, rsLeft, ret,
			style = elem.style;

		computed = computed || getStyles( elem );
		ret = computed ? computed[ name ] : undefined;

		// Avoid setting ret to empty string here
		// so we don't default to auto
		if ( ret == null && style && style[ name ] ) {
			ret = style[ name ];
		}

		// From the awesome hack by Dean Edwards
		// http://erik.eae.net/archives/2007/07/27/18.54.15/#comment-102291

		// If we're not dealing with a regular pixel number
		// but a number that has a weird ending, we need to convert it to pixels
		// but not position css attributes, as those are proportional to the parent element instead
		// and we can't measure the parent instead because it might trigger a "stacking dolls" problem
		if ( rnumnonpx.test( ret ) && !rposition.test( name ) ) {

			// Remember the original values
			left = style.left;
			rs = elem.runtimeStyle;
			rsLeft = rs && rs.left;

			// Put in the new values to get a computed value out
			if ( rsLeft ) {
				rs.left = elem.currentStyle.left;
			}
			style.left = name === "fontSize" ? "1em" : ret;
			ret = style.pixelLeft + "px";

			// Revert the changed values
			style.left = left;
			if ( rsLeft ) {
				rs.left = rsLeft;
			}
		}

		// Support: IE
		// IE returns zIndex value as an integer.
		return ret === undefined ?
			ret :
			ret + "" || "auto";
	};
}




function addGetHookIf( conditionFn, hookFn ) {
	// Define the hook, we'll check on the first run if it's really needed.
	return {
		get: function() {
			var condition = conditionFn();

			if ( condition == null ) {
				// The test was not ready at this point; screw the hook this time
				// but check again when needed next time.
				return;
			}

			if ( condition ) {
				// Hook not needed (or it's not possible to use it due to missing dependency),
				// remove it.
				// Since there are no other hooks for marginRight, remove the whole object.
				delete this.get;
				return;
			}

			// Hook needed; redefine it so that the support test is not executed again.

			return (this.get = hookFn).apply( this, arguments );
		}
	};
}


(function() {
	var a, reliableHiddenOffsetsVal, boxSizingVal, boxSizingReliableVal,
		pixelPositionVal, reliableMarginRightVal,
		div = document.createElement( "div" ),
		containerStyles = "border:0;width:0;height:0;position:absolute;top:0;left:-9999px",
		divReset =
			"-webkit-box-sizing:content-box;-moz-box-sizing:content-box;box-sizing:content-box;" +
			"display:block;padding:0;margin:0;border:0";

	// Setup
	div.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>";
	a = div.getElementsByTagName( "a" )[ 0 ];

	a.style.cssText = "float:left;opacity:.5";

	// Make sure that element opacity exists
	// (IE uses filter instead)
	// Use a regex to work around a WebKit issue. See #5145
	support.opacity = /^0.5/.test( a.style.opacity );

	// Verify style float existence
	// (IE uses styleFloat instead of cssFloat)
	support.cssFloat = !!a.style.cssFloat;

	div.style.backgroundClip = "content-box";
	div.cloneNode( true ).style.backgroundClip = "";
	support.clearCloneStyle = div.style.backgroundClip === "content-box";

	// Null elements to avoid leaks in IE.
	a = div = null;

	jQuery.extend(support, {
		reliableHiddenOffsets: function() {
			if ( reliableHiddenOffsetsVal != null ) {
				return reliableHiddenOffsetsVal;
			}

			var container, tds, isSupported,
				div = document.createElement( "div" ),
				body = document.getElementsByTagName( "body" )[ 0 ];

			if ( !body ) {
				// Return for frameset docs that don't have a body
				return;
			}

			// Setup
			div.setAttribute( "className", "t" );
			div.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>";

			container = document.createElement( "div" );
			container.style.cssText = containerStyles;

			body.appendChild( container ).appendChild( div );

			// Support: IE8
			// Check if table cells still have offsetWidth/Height when they are set
			// to display:none and there are still other visible table cells in a
			// table row; if so, offsetWidth/Height are not reliable for use when
			// determining if an element has been hidden directly using
			// display:none (it is still safe to use offsets if a parent element is
			// hidden; don safety goggles and see bug #4512 for more information).
			div.innerHTML = "<table><tr><td></td><td>t</td></tr></table>";
			tds = div.getElementsByTagName( "td" );
			tds[ 0 ].style.cssText = "padding:0;margin:0;border:0;display:none";
			isSupported = ( tds[ 0 ].offsetHeight === 0 );

			tds[ 0 ].style.display = "";
			tds[ 1 ].style.display = "none";

			// Support: IE8
			// Check if empty table cells still have offsetWidth/Height
			reliableHiddenOffsetsVal = isSupported && ( tds[ 0 ].offsetHeight === 0 );

			body.removeChild( container );

			// Null elements to avoid leaks in IE.
			div = body = null;

			return reliableHiddenOffsetsVal;
		},

		boxSizing: function() {
			if ( boxSizingVal == null ) {
				computeStyleTests();
			}
			return boxSizingVal;
		},

		boxSizingReliable: function() {
			if ( boxSizingReliableVal == null ) {
				computeStyleTests();
			}
			return boxSizingReliableVal;
		},

		pixelPosition: function() {
			if ( pixelPositionVal == null ) {
				computeStyleTests();
			}
			return pixelPositionVal;
		},

		reliableMarginRight: function() {
			var body, container, div, marginDiv;

			// Use window.getComputedStyle because jsdom on node.js will break without it.
			if ( reliableMarginRightVal == null && window.getComputedStyle ) {
				body = document.getElementsByTagName( "body" )[ 0 ];
				if ( !body ) {
					// Test fired too early or in an unsupported environment, exit.
					return;
				}

				container = document.createElement( "div" );
				div = document.createElement( "div" );
				container.style.cssText = containerStyles;

				body.appendChild( container ).appendChild( div );

				// Check if div with explicit width and no margin-right incorrectly
				// gets computed margin-right based on width of container. (#3333)
				// Fails in WebKit before Feb 2011 nightlies
				// WebKit Bug 13343 - getComputedStyle returns wrong value for margin-right
				marginDiv = div.appendChild( document.createElement( "div" ) );
				marginDiv.style.cssText = div.style.cssText = divReset;
				marginDiv.style.marginRight = marginDiv.style.width = "0";
				div.style.width = "1px";

				reliableMarginRightVal =
					!parseFloat( ( window.getComputedStyle( marginDiv, null ) || {} ).marginRight );

				body.removeChild( container );
			}

			return reliableMarginRightVal;
		}
	});

	function computeStyleTests() {
		var container, div,
			body = document.getElementsByTagName( "body" )[ 0 ];

		if ( !body ) {
			// Test fired too early or in an unsupported environment, exit.
			return;
		}

		container = document.createElement( "div" );
		div = document.createElement( "div" );
		container.style.cssText = containerStyles;

		body.appendChild( container ).appendChild( div );

		div.style.cssText =
			"-webkit-box-sizing:border-box;-moz-box-sizing:border-box;box-sizing:border-box;" +
				"position:absolute;display:block;padding:1px;border:1px;width:4px;" +
				"margin-top:1%;top:1%";

		// Workaround failing boxSizing test due to offsetWidth returning wrong value
		// with some non-1 values of body zoom, ticket #13543
		jQuery.swap( body, body.style.zoom != null ? { zoom: 1 } : {}, function() {
			boxSizingVal = div.offsetWidth === 4;
		});

		// Will be changed later if needed.
		boxSizingReliableVal = true;
		pixelPositionVal = false;
		reliableMarginRightVal = true;

		// Use window.getComputedStyle because jsdom on node.js will break without it.
		if ( window.getComputedStyle ) {
			pixelPositionVal = ( window.getComputedStyle( div, null ) || {} ).top !== "1%";
			boxSizingReliableVal =
				( window.getComputedStyle( div, null ) || { width: "4px" } ).width === "4px";
		}

		body.removeChild( container );

		// Null elements to avoid leaks in IE.
		div = body = null;
	}

})();


// A method for quickly swapping in/out CSS properties to get correct calculations.
jQuery.swap = function( elem, options, callback, args ) {
	var ret, name,
		old = {};

	// Remember the old values, and insert the new ones
	for ( name in options ) {
		old[ name ] = elem.style[ name ];
		elem.style[ name ] = options[ name ];
	}

	ret = callback.apply( elem, args || [] );

	// Revert the old values
	for ( name in options ) {
		elem.style[ name ] = old[ name ];
	}

	return ret;
};


var
		ralpha = /alpha\([^)]*\)/i,
	ropacity = /opacity\s*=\s*([^)]*)/,

	// swappable if display is none or starts with table except "table", "table-cell", or "table-caption"
	// see here for display values: https://developer.mozilla.org/en-US/docs/CSS/display
	rdisplayswap = /^(none|table(?!-c[ea]).+)/,
	rnumsplit = new RegExp( "^(" + pnum + ")(.*)$", "i" ),
	rrelNum = new RegExp( "^([+-])=(" + pnum + ")", "i" ),

	cssShow = { position: "absolute", visibility: "hidden", display: "block" },
	cssNormalTransform = {
		letterSpacing: 0,
		fontWeight: 400
	},

	cssPrefixes = [ "Webkit", "O", "Moz", "ms" ];


// return a css property mapped to a potentially vendor prefixed property
function vendorPropName( style, name ) {

	// shortcut for names that are not vendor prefixed
	if ( name in style ) {
		return name;
	}

	// check for vendor prefixed names
	var capName = name.charAt(0).toUpperCase() + name.slice(1),
		origName = name,
		i = cssPrefixes.length;

	while ( i-- ) {
		name = cssPrefixes[ i ] + capName;
		if ( name in style ) {
			return name;
		}
	}

	return origName;
}

function showHide( elements, show ) {
	var display, elem, hidden,
		values = [],
		index = 0,
		length = elements.length;

	for ( ; index < length; index++ ) {
		elem = elements[ index ];
		if ( !elem.style ) {
			continue;
		}

		values[ index ] = jQuery._data( elem, "olddisplay" );
		display = elem.style.display;
		if ( show ) {
			// Reset the inline display of this element to learn if it is
			// being hidden by cascaded rules or not
			if ( !values[ index ] && display === "none" ) {
				elem.style.display = "";
			}

			// Set elements which have been overridden with display: none
			// in a stylesheet to whatever the default browser style is
			// for such an element
			if ( elem.style.display === "" && isHidden( elem ) ) {
				values[ index ] = jQuery._data( elem, "olddisplay", defaultDisplay(elem.nodeName) );
			}
		} else {

			if ( !values[ index ] ) {
				hidden = isHidden( elem );

				if ( display && display !== "none" || !hidden ) {
					jQuery._data( elem, "olddisplay", hidden ? display : jQuery.css( elem, "display" ) );
				}
			}
		}
	}

	// Set the display of most of the elements in a second loop
	// to avoid the constant reflow
	for ( index = 0; index < length; index++ ) {
		elem = elements[ index ];
		if ( !elem.style ) {
			continue;
		}
		if ( !show || elem.style.display === "none" || elem.style.display === "" ) {
			elem.style.display = show ? values[ index ] || "" : "none";
		}
	}

	return elements;
}

function setPositiveNumber( elem, value, subtract ) {
	var matches = rnumsplit.exec( value );
	return matches ?
		// Guard against undefined "subtract", e.g., when used as in cssHooks
		Math.max( 0, matches[ 1 ] - ( subtract || 0 ) ) + ( matches[ 2 ] || "px" ) :
		value;
}

function augmentWidthOrHeight( elem, name, extra, isBorderBox, styles ) {
	var i = extra === ( isBorderBox ? "border" : "content" ) ?
		// If we already have the right measurement, avoid augmentation
		4 :
		// Otherwise initialize for horizontal or vertical properties
		name === "width" ? 1 : 0,

		val = 0;

	for ( ; i < 4; i += 2 ) {
		// both box models exclude margin, so add it if we want it
		if ( extra === "margin" ) {
			val += jQuery.css( elem, extra + cssExpand[ i ], true, styles );
		}

		if ( isBorderBox ) {
			// border-box includes padding, so remove it if we want content
			if ( extra === "content" ) {
				val -= jQuery.css( elem, "padding" + cssExpand[ i ], true, styles );
			}

			// at this point, extra isn't border nor margin, so remove border
			if ( extra !== "margin" ) {
				val -= jQuery.css( elem, "border" + cssExpand[ i ] + "Width", true, styles );
			}
		} else {
			// at this point, extra isn't content, so add padding
			val += jQuery.css( elem, "padding" + cssExpand[ i ], true, styles );

			// at this point, extra isn't content nor padding, so add border
			if ( extra !== "padding" ) {
				val += jQuery.css( elem, "border" + cssExpand[ i ] + "Width", true, styles );
			}
		}
	}

	return val;
}

function getWidthOrHeight( elem, name, extra ) {

	// Start with offset property, which is equivalent to the border-box value
	var valueIsBorderBox = true,
		val = name === "width" ? elem.offsetWidth : elem.offsetHeight,
		styles = getStyles( elem ),
		isBorderBox = support.boxSizing() && jQuery.css( elem, "boxSizing", false, styles ) === "border-box";

	// some non-html elements return undefined for offsetWidth, so check for null/undefined
	// svg - https://bugzilla.mozilla.org/show_bug.cgi?id=649285
	// MathML - https://bugzilla.mozilla.org/show_bug.cgi?id=491668
	if ( val <= 0 || val == null ) {
		// Fall back to computed then uncomputed css if necessary
		val = curCSS( elem, name, styles );
		if ( val < 0 || val == null ) {
			val = elem.style[ name ];
		}

		// Computed unit is not pixels. Stop here and return.
		if ( rnumnonpx.test(val) ) {
			return val;
		}

		// we need the check for style in case a browser which returns unreliable values
		// for getComputedStyle silently falls back to the reliable elem.style
		valueIsBorderBox = isBorderBox && ( support.boxSizingReliable() || val === elem.style[ name ] );

		// Normalize "", auto, and prepare for extra
		val = parseFloat( val ) || 0;
	}

	// use the active box-sizing model to add/subtract irrelevant styles
	return ( val +
		augmentWidthOrHeight(
			elem,
			name,
			extra || ( isBorderBox ? "border" : "content" ),
			valueIsBorderBox,
			styles
		)
	) + "px";
}

jQuery.extend({
	// Add in style property hooks for overriding the default
	// behavior of getting and setting a style property
	cssHooks: {
		opacity: {
			get: function( elem, computed ) {
				if ( computed ) {
					// We should always get a number back from opacity
					var ret = curCSS( elem, "opacity" );
					return ret === "" ? "1" : ret;
				}
			}
		}
	},

	// Don't automatically add "px" to these possibly-unitless properties
	cssNumber: {
		"columnCount": true,
		"fillOpacity": true,
		"fontWeight": true,
		"lineHeight": true,
		"opacity": true,
		"order": true,
		"orphans": true,
		"widows": true,
		"zIndex": true,
		"zoom": true
	},

	// Add in properties whose names you wish to fix before
	// setting or getting the value
	cssProps: {
		// normalize float css property
		"float": support.cssFloat ? "cssFloat" : "styleFloat"
	},

	// Get and set the style property on a DOM Node
	style: function( elem, name, value, extra ) {
		// Don't set styles on text and comment nodes
		if ( !elem || elem.nodeType === 3 || elem.nodeType === 8 || !elem.style ) {
			return;
		}

		// Make sure that we're working with the right name
		var ret, type, hooks,
			origName = jQuery.camelCase( name ),
			style = elem.style;

		name = jQuery.cssProps[ origName ] || ( jQuery.cssProps[ origName ] = vendorPropName( style, origName ) );

		// gets hook for the prefixed version
		// followed by the unprefixed version
		hooks = jQuery.cssHooks[ name ] || jQuery.cssHooks[ origName ];

		// Check if we're setting a value
		if ( value !== undefined ) {
			type = typeof value;

			// convert relative number strings (+= or -=) to relative numbers. #7345
			if ( type === "string" && (ret = rrelNum.exec( value )) ) {
				value = ( ret[1] + 1 ) * ret[2] + parseFloat( jQuery.css( elem, name ) );
				// Fixes bug #9237
				type = "number";
			}

			// Make sure that null and NaN values aren't set. See: #7116
			if ( value == null || value !== value ) {
				return;
			}

			// If a number was passed in, add 'px' to the (except for certain CSS properties)
			if ( type === "number" && !jQuery.cssNumber[ origName ] ) {
				value += "px";
			}

			// Fixes #8908, it can be done more correctly by specifing setters in cssHooks,
			// but it would mean to define eight (for every problematic property) identical functions
			if ( !support.clearCloneStyle && value === "" && name.indexOf("background") === 0 ) {
				style[ name ] = "inherit";
			}

			// If a hook was provided, use that value, otherwise just set the specified value
			if ( !hooks || !("set" in hooks) || (value = hooks.set( elem, value, extra )) !== undefined ) {

				// Support: IE
				// Swallow errors from 'invalid' CSS values (#5509)
				try {
					// Support: Chrome, Safari
					// Setting style to blank string required to delete "style: x !important;"
					style[ name ] = "";
					style[ name ] = value;
				} catch(e) {}
			}

		} else {
			// If a hook was provided get the non-computed value from there
			if ( hooks && "get" in hooks && (ret = hooks.get( elem, false, extra )) !== undefined ) {
				return ret;
			}

			// Otherwise just get the value from the style object
			return style[ name ];
		}
	},

	css: function( elem, name, extra, styles ) {
		var num, val, hooks,
			origName = jQuery.camelCase( name );

		// Make sure that we're working with the right name
		name = jQuery.cssProps[ origName ] || ( jQuery.cssProps[ origName ] = vendorPropName( elem.style, origName ) );

		// gets hook for the prefixed version
		// followed by the unprefixed version
		hooks = jQuery.cssHooks[ name ] || jQuery.cssHooks[ origName ];

		// If a hook was provided get the computed value from there
		if ( hooks && "get" in hooks ) {
			val = hooks.get( elem, true, extra );
		}

		// Otherwise, if a way to get the computed value exists, use that
		if ( val === undefined ) {
			val = curCSS( elem, name, styles );
		}

		//convert "normal" to computed value
		if ( val === "normal" && name in cssNormalTransform ) {
			val = cssNormalTransform[ name ];
		}

		// Return, converting to number if forced or a qualifier was provided and val looks numeric
		if ( extra === "" || extra ) {
			num = parseFloat( val );
			return extra === true || jQuery.isNumeric( num ) ? num || 0 : val;
		}
		return val;
	}
});

jQuery.each([ "height", "width" ], function( i, name ) {
	jQuery.cssHooks[ name ] = {
		get: function( elem, computed, extra ) {
			if ( computed ) {
				// certain elements can have dimension info if we invisibly show them
				// however, it must have a current display style that would benefit from this
				return elem.offsetWidth === 0 && rdisplayswap.test( jQuery.css( elem, "display" ) ) ?
					jQuery.swap( elem, cssShow, function() {
						return getWidthOrHeight( elem, name, extra );
					}) :
					getWidthOrHeight( elem, name, extra );
			}
		},

		set: function( elem, value, extra ) {
			var styles = extra && getStyles( elem );
			return setPositiveNumber( elem, value, extra ?
				augmentWidthOrHeight(
					elem,
					name,
					extra,
					support.boxSizing() && jQuery.css( elem, "boxSizing", false, styles ) === "border-box",
					styles
				) : 0
			);
		}
	};
});

if ( !support.opacity ) {
	jQuery.cssHooks.opacity = {
		get: function( elem, computed ) {
			// IE uses filters for opacity
			return ropacity.test( (computed && elem.currentStyle ? elem.currentStyle.filter : elem.style.filter) || "" ) ?
				( 0.01 * parseFloat( RegExp.$1 ) ) + "" :
				computed ? "1" : "";
		},

		set: function( elem, value ) {
			var style = elem.style,
				currentStyle = elem.currentStyle,
				opacity = jQuery.isNumeric( value ) ? "alpha(opacity=" + value * 100 + ")" : "",
				filter = currentStyle && currentStyle.filter || style.filter || "";

			// IE has trouble with opacity if it does not have layout
			// Force it by setting the zoom level
			style.zoom = 1;

			// if setting opacity to 1, and no other filters exist - attempt to remove filter attribute #6652
			// if value === "", then remove inline opacity #12685
			if ( ( value >= 1 || value === "" ) &&
					jQuery.trim( filter.replace( ralpha, "" ) ) === "" &&
					style.removeAttribute ) {

				// Setting style.filter to null, "" & " " still leave "filter:" in the cssText
				// if "filter:" is present at all, clearType is disabled, we want to avoid this
				// style.removeAttribute is IE Only, but so apparently is this code path...
				style.removeAttribute( "filter" );

				// if there is no filter style applied in a css rule or unset inline opacity, we are done
				if ( value === "" || currentStyle && !currentStyle.filter ) {
					return;
				}
			}

			// otherwise, set new filter values
			style.filter = ralpha.test( filter ) ?
				filter.replace( ralpha, opacity ) :
				filter + " " + opacity;
		}
	};
}

jQuery.cssHooks.marginRight = addGetHookIf( support.reliableMarginRight,
	function( elem, computed ) {
		if ( computed ) {
			// WebKit Bug 13343 - getComputedStyle returns wrong value for margin-right
			// Work around by temporarily setting element display to inline-block
			return jQuery.swap( elem, { "display": "inline-block" },
				curCSS, [ elem, "marginRight" ] );
		}
	}
);

// These hooks are used by animate to expand properties
jQuery.each({
	margin: "",
	padding: "",
	border: "Width"
}, function( prefix, suffix ) {
	jQuery.cssHooks[ prefix + suffix ] = {
		expand: function( value ) {
			var i = 0,
				expanded = {},

				// assumes a single number if not a string
				parts = typeof value === "string" ? value.split(" ") : [ value ];

			for ( ; i < 4; i++ ) {
				expanded[ prefix + cssExpand[ i ] + suffix ] =
					parts[ i ] || parts[ i - 2 ] || parts[ 0 ];
			}

			return expanded;
		}
	};

	if ( !rmargin.test( prefix ) ) {
		jQuery.cssHooks[ prefix + suffix ].set = setPositiveNumber;
	}
});

jQuery.fn.extend({
	css: function( name, value ) {
		return access( this, function( elem, name, value ) {
			var styles, len,
				map = {},
				i = 0;

			if ( jQuery.isArray( name ) ) {
				styles = getStyles( elem );
				len = name.length;

				for ( ; i < len; i++ ) {
					map[ name[ i ] ] = jQuery.css( elem, name[ i ], false, styles );
				}

				return map;
			}

			return value !== undefined ?
				jQuery.style( elem, name, value ) :
				jQuery.css( elem, name );
		}, name, value, arguments.length > 1 );
	},
	show: function() {
		return showHide( this, true );
	},
	hide: function() {
		return showHide( this );
	},
	toggle: function( state ) {
		if ( typeof state === "boolean" ) {
			return state ? this.show() : this.hide();
		}

		return this.each(function() {
			if ( isHidden( this ) ) {
				jQuery( this ).show();
			} else {
				jQuery( this ).hide();
			}
		});
	}
});


function Tween( elem, options, prop, end, easing ) {
	return new Tween.prototype.init( elem, options, prop, end, easing );
}
jQuery.Tween = Tween;

Tween.prototype = {
	constructor: Tween,
	init: function( elem, options, prop, end, easing, unit ) {
		this.elem = elem;
		this.prop = prop;
		this.easing = easing || "swing";
		this.options = options;
		this.start = this.now = this.cur();
		this.end = end;
		this.unit = unit || ( jQuery.cssNumber[ prop ] ? "" : "px" );
	},
	cur: function() {
		var hooks = Tween.propHooks[ this.prop ];

		return hooks && hooks.get ?
			hooks.get( this ) :
			Tween.propHooks._default.get( this );
	},
	run: function( percent ) {
		var eased,
			hooks = Tween.propHooks[ this.prop ];

		if ( this.options.duration ) {
			this.pos = eased = jQuery.easing[ this.easing ](
				percent, this.options.duration * percent, 0, 1, this.options.duration
			);
		} else {
			this.pos = eased = percent;
		}
		this.now = ( this.end - this.start ) * eased + this.start;

		if ( this.options.step ) {
			this.options.step.call( this.elem, this.now, this );
		}

		if ( hooks && hooks.set ) {
			hooks.set( this );
		} else {
			Tween.propHooks._default.set( this );
		}
		return this;
	}
};

Tween.prototype.init.prototype = Tween.prototype;

Tween.propHooks = {
	_default: {
		get: function( tween ) {
			var result;

			if ( tween.elem[ tween.prop ] != null &&
				(!tween.elem.style || tween.elem.style[ tween.prop ] == null) ) {
				return tween.elem[ tween.prop ];
			}

			// passing an empty string as a 3rd parameter to .css will automatically
			// attempt a parseFloat and fallback to a string if the parse fails
			// so, simple values such as "10px" are parsed to Float.
			// complex values such as "rotate(1rad)" are returned as is.
			result = jQuery.css( tween.elem, tween.prop, "" );
			// Empty strings, null, undefined and "auto" are converted to 0.
			return !result || result === "auto" ? 0 : result;
		},
		set: function( tween ) {
			// use step hook for back compat - use cssHook if its there - use .style if its
			// available and use plain properties where available
			if ( jQuery.fx.step[ tween.prop ] ) {
				jQuery.fx.step[ tween.prop ]( tween );
			} else if ( tween.elem.style && ( tween.elem.style[ jQuery.cssProps[ tween.prop ] ] != null || jQuery.cssHooks[ tween.prop ] ) ) {
				jQuery.style( tween.elem, tween.prop, tween.now + tween.unit );
			} else {
				tween.elem[ tween.prop ] = tween.now;
			}
		}
	}
};

// Support: IE <=9
// Panic based approach to setting things on disconnected nodes

Tween.propHooks.scrollTop = Tween.propHooks.scrollLeft = {
	set: function( tween ) {
		if ( tween.elem.nodeType && tween.elem.parentNode ) {
			tween.elem[ tween.prop ] = tween.now;
		}
	}
};

jQuery.easing = {
	linear: function( p ) {
		return p;
	},
	swing: function( p ) {
		return 0.5 - Math.cos( p * Math.PI ) / 2;
	}
};

jQuery.fx = Tween.prototype.init;

// Back Compat <1.8 extension point
jQuery.fx.step = {};




var
	fxNow, timerId,
	rfxtypes = /^(?:toggle|show|hide)$/,
	rfxnum = new RegExp( "^(?:([+-])=|)(" + pnum + ")([a-z%]*)$", "i" ),
	rrun = /queueHooks$/,
	animationPrefilters = [ defaultPrefilter ],
	tweeners = {
		"*": [ function( prop, value ) {
			var tween = this.createTween( prop, value ),
				target = tween.cur(),
				parts = rfxnum.exec( value ),
				unit = parts && parts[ 3 ] || ( jQuery.cssNumber[ prop ] ? "" : "px" ),

				// Starting value computation is required for potential unit mismatches
				start = ( jQuery.cssNumber[ prop ] || unit !== "px" && +target ) &&
					rfxnum.exec( jQuery.css( tween.elem, prop ) ),
				scale = 1,
				maxIterations = 20;

			if ( start && start[ 3 ] !== unit ) {
				// Trust units reported by jQuery.css
				unit = unit || start[ 3 ];

				// Make sure we update the tween properties later on
				parts = parts || [];

				// Iteratively approximate from a nonzero starting point
				start = +target || 1;

				do {
					// If previous iteration zeroed out, double until we get *something*
					// Use a string for doubling factor so we don't accidentally see scale as unchanged below
					scale = scale || ".5";

					// Adjust and apply
					start = start / scale;
					jQuery.style( tween.elem, prop, start + unit );

				// Update scale, tolerating zero or NaN from tween.cur()
				// And breaking the loop if scale is unchanged or perfect, or if we've just had enough
				} while ( scale !== (scale = tween.cur() / target) && scale !== 1 && --maxIterations );
			}

			// Update tween properties
			if ( parts ) {
				start = tween.start = +start || +target || 0;
				tween.unit = unit;
				// If a +=/-= token was provided, we're doing a relative animation
				tween.end = parts[ 1 ] ?
					start + ( parts[ 1 ] + 1 ) * parts[ 2 ] :
					+parts[ 2 ];
			}

			return tween;
		} ]
	};

// Animations created synchronously will run synchronously
function createFxNow() {
	setTimeout(function() {
		fxNow = undefined;
	});
	return ( fxNow = jQuery.now() );
}

// Generate parameters to create a standard animation
function genFx( type, includeWidth ) {
	var which,
		attrs = { height: type },
		i = 0;

	// if we include width, step value is 1 to do all cssExpand values,
	// if we don't include width, step value is 2 to skip over Left and Right
	includeWidth = includeWidth ? 1 : 0;
	for ( ; i < 4 ; i += 2 - includeWidth ) {
		which = cssExpand[ i ];
		attrs[ "margin" + which ] = attrs[ "padding" + which ] = type;
	}

	if ( includeWidth ) {
		attrs.opacity = attrs.width = type;
	}

	return attrs;
}

function createTween( value, prop, animation ) {
	var tween,
		collection = ( tweeners[ prop ] || [] ).concat( tweeners[ "*" ] ),
		index = 0,
		length = collection.length;
	for ( ; index < length; index++ ) {
		if ( (tween = collection[ index ].call( animation, prop, value )) ) {

			// we're done with this property
			return tween;
		}
	}
}

function defaultPrefilter( elem, props, opts ) {
	/* jshint validthis: true */
	var prop, value, toggle, tween, hooks, oldfire, display, dDisplay,
		anim = this,
		orig = {},
		style = elem.style,
		hidden = elem.nodeType && isHidden( elem ),
		dataShow = jQuery._data( elem, "fxshow" );

	// handle queue: false promises
	if ( !opts.queue ) {
		hooks = jQuery._queueHooks( elem, "fx" );
		if ( hooks.unqueued == null ) {
			hooks.unqueued = 0;
			oldfire = hooks.empty.fire;
			hooks.empty.fire = function() {
				if ( !hooks.unqueued ) {
					oldfire();
				}
			};
		}
		hooks.unqueued++;

		anim.always(function() {
			// doing this makes sure that the complete handler will be called
			// before this completes
			anim.always(function() {
				hooks.unqueued--;
				if ( !jQuery.queue( elem, "fx" ).length ) {
					hooks.empty.fire();
				}
			});
		});
	}

	// height/width overflow pass
	if ( elem.nodeType === 1 && ( "height" in props || "width" in props ) ) {
		// Make sure that nothing sneaks out
		// Record all 3 overflow attributes because IE does not
		// change the overflow attribute when overflowX and
		// overflowY are set to the same value
		opts.overflow = [ style.overflow, style.overflowX, style.overflowY ];

		// Set display property to inline-block for height/width
		// animations on inline elements that are having width/height animated
		display = jQuery.css( elem, "display" );
		dDisplay = defaultDisplay( elem.nodeName );
		if ( display === "none" ) {
			display = dDisplay;
		}
		if ( display === "inline" &&
				jQuery.css( elem, "float" ) === "none" ) {

			// inline-level elements accept inline-block;
			// block-level elements need to be inline with layout
			if ( !support.inlineBlockNeedsLayout || dDisplay === "inline" ) {
				style.display = "inline-block";
			} else {
				style.zoom = 1;
			}
		}
	}

	if ( opts.overflow ) {
		style.overflow = "hidden";
		if ( !support.shrinkWrapBlocks() ) {
			anim.always(function() {
				style.overflow = opts.overflow[ 0 ];
				style.overflowX = opts.overflow[ 1 ];
				style.overflowY = opts.overflow[ 2 ];
			});
		}
	}

	// show/hide pass
	for ( prop in props ) {
		value = props[ prop ];
		if ( rfxtypes.exec( value ) ) {
			delete props[ prop ];
			toggle = toggle || value === "toggle";
			if ( value === ( hidden ? "hide" : "show" ) ) {

				// If there is dataShow left over from a stopped hide or show and we are going to proceed with show, we should pretend to be hidden
				if ( value === "show" && dataShow && dataShow[ prop ] !== undefined ) {
					hidden = true;
				} else {
					continue;
				}
			}
			orig[ prop ] = dataShow && dataShow[ prop ] || jQuery.style( elem, prop );
		}
	}

	if ( !jQuery.isEmptyObject( orig ) ) {
		if ( dataShow ) {
			if ( "hidden" in dataShow ) {
				hidden = dataShow.hidden;
			}
		} else {
			dataShow = jQuery._data( elem, "fxshow", {} );
		}

		// store state if its toggle - enables .stop().toggle() to "reverse"
		if ( toggle ) {
			dataShow.hidden = !hidden;
		}
		if ( hidden ) {
			jQuery( elem ).show();
		} else {
			anim.done(function() {
				jQuery( elem ).hide();
			});
		}
		anim.done(function() {
			var prop;
			jQuery._removeData( elem, "fxshow" );
			for ( prop in orig ) {
				jQuery.style( elem, prop, orig[ prop ] );
			}
		});
		for ( prop in orig ) {
			tween = createTween( hidden ? dataShow[ prop ] : 0, prop, anim );

			if ( !( prop in dataShow ) ) {
				dataShow[ prop ] = tween.start;
				if ( hidden ) {
					tween.end = tween.start;
					tween.start = prop === "width" || prop === "height" ? 1 : 0;
				}
			}
		}
	}
}

function propFilter( props, specialEasing ) {
	var index, name, easing, value, hooks;

	// camelCase, specialEasing and expand cssHook pass
	for ( index in props ) {
		name = jQuery.camelCase( index );
		easing = specialEasing[ name ];
		value = props[ index ];
		if ( jQuery.isArray( value ) ) {
			easing = value[ 1 ];
			value = props[ index ] = value[ 0 ];
		}

		if ( index !== name ) {
			props[ name ] = value;
			delete props[ index ];
		}

		hooks = jQuery.cssHooks[ name ];
		if ( hooks && "expand" in hooks ) {
			value = hooks.expand( value );
			delete props[ name ];

			// not quite $.extend, this wont overwrite keys already present.
			// also - reusing 'index' from above because we have the correct "name"
			for ( index in value ) {
				if ( !( index in props ) ) {
					props[ index ] = value[ index ];
					specialEasing[ index ] = easing;
				}
			}
		} else {
			specialEasing[ name ] = easing;
		}
	}
}

function Animation( elem, properties, options ) {
	var result,
		stopped,
		index = 0,
		length = animationPrefilters.length,
		deferred = jQuery.Deferred().always( function() {
			// don't match elem in the :animated selector
			delete tick.elem;
		}),
		tick = function() {
			if ( stopped ) {
				return false;
			}
			var currentTime = fxNow || createFxNow(),
				remaining = Math.max( 0, animation.startTime + animation.duration - currentTime ),
				// archaic crash bug won't allow us to use 1 - ( 0.5 || 0 ) (#12497)
				temp = remaining / animation.duration || 0,
				percent = 1 - temp,
				index = 0,
				length = animation.tweens.length;

			for ( ; index < length ; index++ ) {
				animation.tweens[ index ].run( percent );
			}

			deferred.notifyWith( elem, [ animation, percent, remaining ]);

			if ( percent < 1 && length ) {
				return remaining;
			} else {
				deferred.resolveWith( elem, [ animation ] );
				return false;
			}
		},
		animation = deferred.promise({
			elem: elem,
			props: jQuery.extend( {}, properties ),
			opts: jQuery.extend( true, { specialEasing: {} }, options ),
			originalProperties: properties,
			originalOptions: options,
			startTime: fxNow || createFxNow(),
			duration: options.duration,
			tweens: [],
			createTween: function( prop, end ) {
				var tween = jQuery.Tween( elem, animation.opts, prop, end,
						animation.opts.specialEasing[ prop ] || animation.opts.easing );
				animation.tweens.push( tween );
				return tween;
			},
			stop: function( gotoEnd ) {
				var index = 0,
					// if we are going to the end, we want to run all the tweens
					// otherwise we skip this part
					length = gotoEnd ? animation.tweens.length : 0;
				if ( stopped ) {
					return this;
				}
				stopped = true;
				for ( ; index < length ; index++ ) {
					animation.tweens[ index ].run( 1 );
				}

				// resolve when we played the last frame
				// otherwise, reject
				if ( gotoEnd ) {
					deferred.resolveWith( elem, [ animation, gotoEnd ] );
				} else {
					deferred.rejectWith( elem, [ animation, gotoEnd ] );
				}
				return this;
			}
		}),
		props = animation.props;

	propFilter( props, animation.opts.specialEasing );

	for ( ; index < length ; index++ ) {
		result = animationPrefilters[ index ].call( animation, elem, props, animation.opts );
		if ( result ) {
			return result;
		}
	}

	jQuery.map( props, createTween, animation );

	if ( jQuery.isFunction( animation.opts.start ) ) {
		animation.opts.start.call( elem, animation );
	}

	jQuery.fx.timer(
		jQuery.extend( tick, {
			elem: elem,
			anim: animation,
			queue: animation.opts.queue
		})
	);

	// attach callbacks from options
	return animation.progress( animation.opts.progress )
		.done( animation.opts.done, animation.opts.complete )
		.fail( animation.opts.fail )
		.always( animation.opts.always );
}

jQuery.Animation = jQuery.extend( Animation, {
	tweener: function( props, callback ) {
		if ( jQuery.isFunction( props ) ) {
			callback = props;
			props = [ "*" ];
		} else {
			props = props.split(" ");
		}

		var prop,
			index = 0,
			length = props.length;

		for ( ; index < length ; index++ ) {
			prop = props[ index ];
			tweeners[ prop ] = tweeners[ prop ] || [];
			tweeners[ prop ].unshift( callback );
		}
	},

	prefilter: function( callback, prepend ) {
		if ( prepend ) {
			animationPrefilters.unshift( callback );
		} else {
			animationPrefilters.push( callback );
		}
	}
});

jQuery.speed = function( speed, easing, fn ) {
	var opt = speed && typeof speed === "object" ? jQuery.extend( {}, speed ) : {
		complete: fn || !fn && easing ||
			jQuery.isFunction( speed ) && speed,
		duration: speed,
		easing: fn && easing || easing && !jQuery.isFunction( easing ) && easing
	};

	opt.duration = jQuery.fx.off ? 0 : typeof opt.duration === "number" ? opt.duration :
		opt.duration in jQuery.fx.speeds ? jQuery.fx.speeds[ opt.duration ] : jQuery.fx.speeds._default;

	// normalize opt.queue - true/undefined/null -> "fx"
	if ( opt.queue == null || opt.queue === true ) {
		opt.queue = "fx";
	}

	// Queueing
	opt.old = opt.complete;

	opt.complete = function() {
		if ( jQuery.isFunction( opt.old ) ) {
			opt.old.call( this );
		}

		if ( opt.queue ) {
			jQuery.dequeue( this, opt.queue );
		}
	};

	return opt;
};

jQuery.fn.extend({
	fadeTo: function( speed, to, easing, callback ) {

		// show any hidden elements after setting opacity to 0
		return this.filter( isHidden ).css( "opacity", 0 ).show()

			// animate to the value specified
			.end().animate({ opacity: to }, speed, easing, callback );
	},
	animate: function( prop, speed, easing, callback ) {
		var empty = jQuery.isEmptyObject( prop ),
			optall = jQuery.speed( speed, easing, callback ),
			doAnimation = function() {
				// Operate on a copy of prop so per-property easing won't be lost
				var anim = Animation( this, jQuery.extend( {}, prop ), optall );

				// Empty animations, or finishing resolves immediately
				if ( empty || jQuery._data( this, "finish" ) ) {
					anim.stop( true );
				}
			};
			doAnimation.finish = doAnimation;

		return empty || optall.queue === false ?
			this.each( doAnimation ) :
			this.queue( optall.queue, doAnimation );
	},
	stop: function( type, clearQueue, gotoEnd ) {
		var stopQueue = function( hooks ) {
			var stop = hooks.stop;
			delete hooks.stop;
			stop( gotoEnd );
		};

		if ( typeof type !== "string" ) {
			gotoEnd = clearQueue;
			clearQueue = type;
			type = undefined;
		}
		if ( clearQueue && type !== false ) {
			this.queue( type || "fx", [] );
		}

		return this.each(function() {
			var dequeue = true,
				index = type != null && type + "queueHooks",
				timers = jQuery.timers,
				data = jQuery._data( this );

			if ( index ) {
				if ( data[ index ] && data[ index ].stop ) {
					stopQueue( data[ index ] );
				}
			} else {
				for ( index in data ) {
					if ( data[ index ] && data[ index ].stop && rrun.test( index ) ) {
						stopQueue( data[ index ] );
					}
				}
			}

			for ( index = timers.length; index--; ) {
				if ( timers[ index ].elem === this && (type == null || timers[ index ].queue === type) ) {
					timers[ index ].anim.stop( gotoEnd );
					dequeue = false;
					timers.splice( index, 1 );
				}
			}

			// start the next in the queue if the last step wasn't forced
			// timers currently will call their complete callbacks, which will dequeue
			// but only if they were gotoEnd
			if ( dequeue || !gotoEnd ) {
				jQuery.dequeue( this, type );
			}
		});
	},
	finish: function( type ) {
		if ( type !== false ) {
			type = type || "fx";
		}
		return this.each(function() {
			var index,
				data = jQuery._data( this ),
				queue = data[ type + "queue" ],
				hooks = data[ type + "queueHooks" ],
				timers = jQuery.timers,
				length = queue ? queue.length : 0;

			// enable finishing flag on private data
			data.finish = true;

			// empty the queue first
			jQuery.queue( this, type, [] );

			if ( hooks && hooks.stop ) {
				hooks.stop.call( this, true );
			}

			// look for any active animations, and finish them
			for ( index = timers.length; index--; ) {
				if ( timers[ index ].elem === this && timers[ index ].queue === type ) {
					timers[ index ].anim.stop( true );
					timers.splice( index, 1 );
				}
			}

			// look for any animations in the old queue and finish them
			for ( index = 0; index < length; index++ ) {
				if ( queue[ index ] && queue[ index ].finish ) {
					queue[ index ].finish.call( this );
				}
			}

			// turn off finishing flag
			delete data.finish;
		});
	}
});

jQuery.each([ "toggle", "show", "hide" ], function( i, name ) {
	var cssFn = jQuery.fn[ name ];
	jQuery.fn[ name ] = function( speed, easing, callback ) {
		return speed == null || typeof speed === "boolean" ?
			cssFn.apply( this, arguments ) :
			this.animate( genFx( name, true ), speed, easing, callback );
	};
});

// Generate shortcuts for custom animations
jQuery.each({
	slideDown: genFx("show"),
	slideUp: genFx("hide"),
	slideToggle: genFx("toggle"),
	fadeIn: { opacity: "show" },
	fadeOut: { opacity: "hide" },
	fadeToggle: { opacity: "toggle" }
}, function( name, props ) {
	jQuery.fn[ name ] = function( speed, easing, callback ) {
		return this.animate( props, speed, easing, callback );
	};
});

jQuery.timers = [];
jQuery.fx.tick = function() {
	var timer,
		timers = jQuery.timers,
		i = 0;

	fxNow = jQuery.now();

	for ( ; i < timers.length; i++ ) {
		timer = timers[ i ];
		// Checks the timer has not already been removed
		if ( !timer() && timers[ i ] === timer ) {
			timers.splice( i--, 1 );
		}
	}

	if ( !timers.length ) {
		jQuery.fx.stop();
	}
	fxNow = undefined;
};

jQuery.fx.timer = function( timer ) {
	jQuery.timers.push( timer );
	if ( timer() ) {
		jQuery.fx.start();
	} else {
		jQuery.timers.pop();
	}
};

jQuery.fx.interval = 13;

jQuery.fx.start = function() {
	if ( !timerId ) {
		timerId = setInterval( jQuery.fx.tick, jQuery.fx.interval );
	}
};

jQuery.fx.stop = function() {
	clearInterval( timerId );
	timerId = null;
};

jQuery.fx.speeds = {
	slow: 600,
	fast: 200,
	// Default speed
	_default: 400
};


// Based off of the plugin by Clint Helfers, with permission.
// http://blindsignals.com/index.php/2009/07/jquery-delay/
jQuery.fn.delay = function( time, type ) {
	time = jQuery.fx ? jQuery.fx.speeds[ time ] || time : time;
	type = type || "fx";

	return this.queue( type, function( next, hooks ) {
		var timeout = setTimeout( next, time );
		hooks.stop = function() {
			clearTimeout( timeout );
		};
	});
};


(function() {
	var a, input, select, opt,
		div = document.createElement("div" );

	// Setup
	div.setAttribute( "className", "t" );
	div.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>";
	a = div.getElementsByTagName("a")[ 0 ];

	// First batch of tests.
	select = document.createElement("select");
	opt = select.appendChild( document.createElement("option") );
	input = div.getElementsByTagName("input")[ 0 ];

	a.style.cssText = "top:1px";

	// Test setAttribute on camelCase class. If it works, we need attrFixes when doing get/setAttribute (ie6/7)
	support.getSetAttribute = div.className !== "t";

	// Get the style information from getAttribute
	// (IE uses .cssText instead)
	support.style = /top/.test( a.getAttribute("style") );

	// Make sure that URLs aren't manipulated
	// (IE normalizes it by default)
	support.hrefNormalized = a.getAttribute("href") === "/a";

	// Check the default checkbox/radio value ("" on WebKit; "on" elsewhere)
	support.checkOn = !!input.value;

	// Make sure that a selected-by-default option has a working selected property.
	// (WebKit defaults to false instead of true, IE too, if it's in an optgroup)
	support.optSelected = opt.selected;

	// Tests for enctype support on a form (#6743)
	support.enctype = !!document.createElement("form").enctype;

	// Make sure that the options inside disabled selects aren't marked as disabled
	// (WebKit marks them as disabled)
	select.disabled = true;
	support.optDisabled = !opt.disabled;

	// Support: IE8 only
	// Check if we can trust getAttribute("value")
	input = document.createElement( "input" );
	input.setAttribute( "value", "" );
	support.input = input.getAttribute( "value" ) === "";

	// Check if an input maintains its value after becoming a radio
	input.value = "t";
	input.setAttribute( "type", "radio" );
	support.radioValue = input.value === "t";

	// Null elements to avoid leaks in IE.
	a = input = select = opt = div = null;
})();


var rreturn = /\r/g;

jQuery.fn.extend({
	val: function( value ) {
		var hooks, ret, isFunction,
			elem = this[0];

		if ( !arguments.length ) {
			if ( elem ) {
				hooks = jQuery.valHooks[ elem.type ] || jQuery.valHooks[ elem.nodeName.toLowerCase() ];

				if ( hooks && "get" in hooks && (ret = hooks.get( elem, "value" )) !== undefined ) {
					return ret;
				}

				ret = elem.value;

				return typeof ret === "string" ?
					// handle most common string cases
					ret.replace(rreturn, "") :
					// handle cases where value is null/undef or number
					ret == null ? "" : ret;
			}

			return;
		}

		isFunction = jQuery.isFunction( value );

		return this.each(function( i ) {
			var val;

			if ( this.nodeType !== 1 ) {
				return;
			}

			if ( isFunction ) {
				val = value.call( this, i, jQuery( this ).val() );
			} else {
				val = value;
			}

			// Treat null/undefined as ""; convert numbers to string
			if ( val == null ) {
				val = "";
			} else if ( typeof val === "number" ) {
				val += "";
			} else if ( jQuery.isArray( val ) ) {
				val = jQuery.map( val, function( value ) {
					return value == null ? "" : value + "";
				});
			}

			hooks = jQuery.valHooks[ this.type ] || jQuery.valHooks[ this.nodeName.toLowerCase() ];

			// If set returns undefined, fall back to normal setting
			if ( !hooks || !("set" in hooks) || hooks.set( this, val, "value" ) === undefined ) {
				this.value = val;
			}
		});
	}
});

jQuery.extend({
	valHooks: {
		option: {
			get: function( elem ) {
				var val = jQuery.find.attr( elem, "value" );
				return val != null ?
					val :
					jQuery.text( elem );
			}
		},
		select: {
			get: function( elem ) {
				var value, option,
					options = elem.options,
					index = elem.selectedIndex,
					one = elem.type === "select-one" || index < 0,
					values = one ? null : [],
					max = one ? index + 1 : options.length,
					i = index < 0 ?
						max :
						one ? index : 0;

				// Loop through all the selected options
				for ( ; i < max; i++ ) {
					option = options[ i ];

					// oldIE doesn't update selected after form reset (#2551)
					if ( ( option.selected || i === index ) &&
							// Don't return options that are disabled or in a disabled optgroup
							( support.optDisabled ? !option.disabled : option.getAttribute("disabled") === null ) &&
							( !option.parentNode.disabled || !jQuery.nodeName( option.parentNode, "optgroup" ) ) ) {

						// Get the specific value for the option
						value = jQuery( option ).val();

						// We don't need an array for one selects
						if ( one ) {
							return value;
						}

						// Multi-Selects return an array
						values.push( value );
					}
				}

				return values;
			},

			set: function( elem, value ) {
				var optionSet, option,
					options = elem.options,
					values = jQuery.makeArray( value ),
					i = options.length;

				while ( i-- ) {
					option = options[ i ];

					if ( jQuery.inArray( jQuery.valHooks.option.get( option ), values ) >= 0 ) {

						// Support: IE6
						// When new option element is added to select box we need to
						// force reflow of newly added node in order to workaround delay
						// of initialization properties
						try {
							option.selected = optionSet = true;

						} catch ( _ ) {

							// Will be executed only in IE6
							option.scrollHeight;
						}

					} else {
						option.selected = false;
					}
				}

				// Force browsers to behave consistently when non-matching value is set
				if ( !optionSet ) {
					elem.selectedIndex = -1;
				}

				return options;
			}
		}
	}
});

// Radios and checkboxes getter/setter
jQuery.each([ "radio", "checkbox" ], function() {
	jQuery.valHooks[ this ] = {
		set: function( elem, value ) {
			if ( jQuery.isArray( value ) ) {
				return ( elem.checked = jQuery.inArray( jQuery(elem).val(), value ) >= 0 );
			}
		}
	};
	if ( !support.checkOn ) {
		jQuery.valHooks[ this ].get = function( elem ) {
			// Support: Webkit
			// "" is returned instead of "on" if a value isn't specified
			return elem.getAttribute("value") === null ? "on" : elem.value;
		};
	}
});




var nodeHook, boolHook,
	attrHandle = jQuery.expr.attrHandle,
	ruseDefault = /^(?:checked|selected)$/i,
	getSetAttribute = support.getSetAttribute,
	getSetInput = support.input;

jQuery.fn.extend({
	attr: function( name, value ) {
		return access( this, jQuery.attr, name, value, arguments.length > 1 );
	},

	removeAttr: function( name ) {
		return this.each(function() {
			jQuery.removeAttr( this, name );
		});
	}
});

jQuery.extend({
	attr: function( elem, name, value ) {
		var hooks, ret,
			nType = elem.nodeType;

		// don't get/set attributes on text, comment and attribute nodes
		if ( !elem || nType === 3 || nType === 8 || nType === 2 ) {
			return;
		}

		// Fallback to prop when attributes are not supported
		if ( typeof elem.getAttribute === strundefined ) {
			return jQuery.prop( elem, name, value );
		}

		// All attributes are lowercase
		// Grab necessary hook if one is defined
		if ( nType !== 1 || !jQuery.isXMLDoc( elem ) ) {
			name = name.toLowerCase();
			hooks = jQuery.attrHooks[ name ] ||
				( jQuery.expr.match.bool.test( name ) ? boolHook : nodeHook );
		}

		if ( value !== undefined ) {

			if ( value === null ) {
				jQuery.removeAttr( elem, name );

			} else if ( hooks && "set" in hooks && (ret = hooks.set( elem, value, name )) !== undefined ) {
				return ret;

			} else {
				elem.setAttribute( name, value + "" );
				return value;
			}

		} else if ( hooks && "get" in hooks && (ret = hooks.get( elem, name )) !== null ) {
			return ret;

		} else {
			ret = jQuery.find.attr( elem, name );

			// Non-existent attributes return null, we normalize to undefined
			return ret == null ?
				undefined :
				ret;
		}
	},

	removeAttr: function( elem, value ) {
		var name, propName,
			i = 0,
			attrNames = value && value.match( rnotwhite );

		if ( attrNames && elem.nodeType === 1 ) {
			while ( (name = attrNames[i++]) ) {
				propName = jQuery.propFix[ name ] || name;

				// Boolean attributes get special treatment (#10870)
				if ( jQuery.expr.match.bool.test( name ) ) {
					// Set corresponding property to false
					if ( getSetInput && getSetAttribute || !ruseDefault.test( name ) ) {
						elem[ propName ] = false;
					// Support: IE<9
					// Also clear defaultChecked/defaultSelected (if appropriate)
					} else {
						elem[ jQuery.camelCase( "default-" + name ) ] =
							elem[ propName ] = false;
					}

				// See #9699 for explanation of this approach (setting first, then removal)
				} else {
					jQuery.attr( elem, name, "" );
				}

				elem.removeAttribute( getSetAttribute ? name : propName );
			}
		}
	},

	attrHooks: {
		type: {
			set: function( elem, value ) {
				if ( !support.radioValue && value === "radio" && jQuery.nodeName(elem, "input") ) {
					// Setting the type on a radio button after the value resets the value in IE6-9
					// Reset value to default in case type is set after value during creation
					var val = elem.value;
					elem.setAttribute( "type", value );
					if ( val ) {
						elem.value = val;
					}
					return value;
				}
			}
		}
	}
});

// Hook for boolean attributes
boolHook = {
	set: function( elem, value, name ) {
		if ( value === false ) {
			// Remove boolean attributes when set to false
			jQuery.removeAttr( elem, name );
		} else if ( getSetInput && getSetAttribute || !ruseDefault.test( name ) ) {
			// IE<8 needs the *property* name
			elem.setAttribute( !getSetAttribute && jQuery.propFix[ name ] || name, name );

		// Use defaultChecked and defaultSelected for oldIE
		} else {
			elem[ jQuery.camelCase( "default-" + name ) ] = elem[ name ] = true;
		}

		return name;
	}
};

// Retrieve booleans specially
jQuery.each( jQuery.expr.match.bool.source.match( /\w+/g ), function( i, name ) {

	var getter = attrHandle[ name ] || jQuery.find.attr;

	attrHandle[ name ] = getSetInput && getSetAttribute || !ruseDefault.test( name ) ?
		function( elem, name, isXML ) {
			var ret, handle;
			if ( !isXML ) {
				// Avoid an infinite loop by temporarily removing this function from the getter
				handle = attrHandle[ name ];
				attrHandle[ name ] = ret;
				ret = getter( elem, name, isXML ) != null ?
					name.toLowerCase() :
					null;
				attrHandle[ name ] = handle;
			}
			return ret;
		} :
		function( elem, name, isXML ) {
			if ( !isXML ) {
				return elem[ jQuery.camelCase( "default-" + name ) ] ?
					name.toLowerCase() :
					null;
			}
		};
});

// fix oldIE attroperties
if ( !getSetInput || !getSetAttribute ) {
	jQuery.attrHooks.value = {
		set: function( elem, value, name ) {
			if ( jQuery.nodeName( elem, "input" ) ) {
				// Does not return so that setAttribute is also used
				elem.defaultValue = value;
			} else {
				// Use nodeHook if defined (#1954); otherwise setAttribute is fine
				return nodeHook && nodeHook.set( elem, value, name );
			}
		}
	};
}

// IE6/7 do not support getting/setting some attributes with get/setAttribute
if ( !getSetAttribute ) {

	// Use this for any attribute in IE6/7
	// This fixes almost every IE6/7 issue
	nodeHook = {
		set: function( elem, value, name ) {
			// Set the existing or create a new attribute node
			var ret = elem.getAttributeNode( name );
			if ( !ret ) {
				elem.setAttributeNode(
					(ret = elem.ownerDocument.createAttribute( name ))
				);
			}

			ret.value = value += "";

			// Break association with cloned elements by also using setAttribute (#9646)
			if ( name === "value" || value === elem.getAttribute( name ) ) {
				return value;
			}
		}
	};

	// Some attributes are constructed with empty-string values when not defined
	attrHandle.id = attrHandle.name = attrHandle.coords =
		function( elem, name, isXML ) {
			var ret;
			if ( !isXML ) {
				return (ret = elem.getAttributeNode( name )) && ret.value !== "" ?
					ret.value :
					null;
			}
		};

	// Fixing value retrieval on a button requires this module
	jQuery.valHooks.button = {
		get: function( elem, name ) {
			var ret = elem.getAttributeNode( name );
			if ( ret && ret.specified ) {
				return ret.value;
			}
		},
		set: nodeHook.set
	};

	// Set contenteditable to false on removals(#10429)
	// Setting to empty string throws an error as an invalid value
	jQuery.attrHooks.contenteditable = {
		set: function( elem, value, name ) {
			nodeHook.set( elem, value === "" ? false : value, name );
		}
	};

	// Set width and height to auto instead of 0 on empty string( Bug #8150 )
	// This is for removals
	jQuery.each([ "width", "height" ], function( i, name ) {
		jQuery.attrHooks[ name ] = {
			set: function( elem, value ) {
				if ( value === "" ) {
					elem.setAttribute( name, "auto" );
					return value;
				}
			}
		};
	});
}

if ( !support.style ) {
	jQuery.attrHooks.style = {
		get: function( elem ) {
			// Return undefined in the case of empty string
			// Note: IE uppercases css property names, but if we were to .toLowerCase()
			// .cssText, that would destroy case senstitivity in URL's, like in "background"
			return elem.style.cssText || undefined;
		},
		set: function( elem, value ) {
			return ( elem.style.cssText = value + "" );
		}
	};
}




var rfocusable = /^(?:input|select|textarea|button|object)$/i,
	rclickable = /^(?:a|area)$/i;

jQuery.fn.extend({
	prop: function( name, value ) {
		return access( this, jQuery.prop, name, value, arguments.length > 1 );
	},

	removeProp: function( name ) {
		name = jQuery.propFix[ name ] || name;
		return this.each(function() {
			// try/catch handles cases where IE balks (such as removing a property on window)
			try {
				this[ name ] = undefined;
				delete this[ name ];
			} catch( e ) {}
		});
	}
});

jQuery.extend({
	propFix: {
		"for": "htmlFor",
		"class": "className"
	},

	prop: function( elem, name, value ) {
		var ret, hooks, notxml,
			nType = elem.nodeType;

		// don't get/set properties on text, comment and attribute nodes
		if ( !elem || nType === 3 || nType === 8 || nType === 2 ) {
			return;
		}

		notxml = nType !== 1 || !jQuery.isXMLDoc( elem );

		if ( notxml ) {
			// Fix name and attach hooks
			name = jQuery.propFix[ name ] || name;
			hooks = jQuery.propHooks[ name ];
		}

		if ( value !== undefined ) {
			return hooks && "set" in hooks && (ret = hooks.set( elem, value, name )) !== undefined ?
				ret :
				( elem[ name ] = value );

		} else {
			return hooks && "get" in hooks && (ret = hooks.get( elem, name )) !== null ?
				ret :
				elem[ name ];
		}
	},

	propHooks: {
		tabIndex: {
			get: function( elem ) {
				// elem.tabIndex doesn't always return the correct value when it hasn't been explicitly set
				// http://fluidproject.org/blog/2008/01/09/getting-setting-and-removing-tabindex-values-with-javascript/
				// Use proper attribute retrieval(#12072)
				var tabindex = jQuery.find.attr( elem, "tabindex" );

				return tabindex ?
					parseInt( tabindex, 10 ) :
					rfocusable.test( elem.nodeName ) || rclickable.test( elem.nodeName ) && elem.href ?
						0 :
						-1;
			}
		}
	}
});

// Some attributes require a special call on IE
// http://msdn.microsoft.com/en-us/library/ms536429%28VS.85%29.aspx
if ( !support.hrefNormalized ) {
	// href/src property should get the full normalized URL (#10299/#12915)
	jQuery.each([ "href", "src" ], function( i, name ) {
		jQuery.propHooks[ name ] = {
			get: function( elem ) {
				return elem.getAttribute( name, 4 );
			}
		};
	});
}

// Support: Safari, IE9+
// mis-reports the default selected property of an option
// Accessing the parent's selectedIndex property fixes it
if ( !support.optSelected ) {
	jQuery.propHooks.selected = {
		get: function( elem ) {
			var parent = elem.parentNode;

			if ( parent ) {
				parent.selectedIndex;

				// Make sure that it also works with optgroups, see #5701
				if ( parent.parentNode ) {
					parent.parentNode.selectedIndex;
				}
			}
			return null;
		}
	};
}

jQuery.each([
	"tabIndex",
	"readOnly",
	"maxLength",
	"cellSpacing",
	"cellPadding",
	"rowSpan",
	"colSpan",
	"useMap",
	"frameBorder",
	"contentEditable"
], function() {
	jQuery.propFix[ this.toLowerCase() ] = this;
});

// IE6/7 call enctype encoding
if ( !support.enctype ) {
	jQuery.propFix.enctype = "encoding";
}




var rclass = /[\t\r\n\f]/g;

jQuery.fn.extend({
	addClass: function( value ) {
		var classes, elem, cur, clazz, j, finalValue,
			i = 0,
			len = this.length,
			proceed = typeof value === "string" && value;

		if ( jQuery.isFunction( value ) ) {
			return this.each(function( j ) {
				jQuery( this ).addClass( value.call( this, j, this.className ) );
			});
		}

		if ( proceed ) {
			// The disjunction here is for better compressibility (see removeClass)
			classes = ( value || "" ).match( rnotwhite ) || [];

			for ( ; i < len; i++ ) {
				elem = this[ i ];
				cur = elem.nodeType === 1 && ( elem.className ?
					( " " + elem.className + " " ).replace( rclass, " " ) :
					" "
				);

				if ( cur ) {
					j = 0;
					while ( (clazz = classes[j++]) ) {
						if ( cur.indexOf( " " + clazz + " " ) < 0 ) {
							cur += clazz + " ";
						}
					}

					// only assign if different to avoid unneeded rendering.
					finalValue = jQuery.trim( cur );
					if ( elem.className !== finalValue ) {
						elem.className = finalValue;
					}
				}
			}
		}

		return this;
	},

	removeClass: function( value ) {
		var classes, elem, cur, clazz, j, finalValue,
			i = 0,
			len = this.length,
			proceed = arguments.length === 0 || typeof value === "string" && value;

		if ( jQuery.isFunction( value ) ) {
			return this.each(function( j ) {
				jQuery( this ).removeClass( value.call( this, j, this.className ) );
			});
		}
		if ( proceed ) {
			classes = ( value || "" ).match( rnotwhite ) || [];

			for ( ; i < len; i++ ) {
				elem = this[ i ];
				// This expression is here for better compressibility (see addClass)
				cur = elem.nodeType === 1 && ( elem.className ?
					( " " + elem.className + " " ).replace( rclass, " " ) :
					""
				);

				if ( cur ) {
					j = 0;
					while ( (clazz = classes[j++]) ) {
						// Remove *all* instances
						while ( cur.indexOf( " " + clazz + " " ) >= 0 ) {
							cur = cur.replace( " " + clazz + " ", " " );
						}
					}

					// only assign if different to avoid unneeded rendering.
					finalValue = value ? jQuery.trim( cur ) : "";
					if ( elem.className !== finalValue ) {
						elem.className = finalValue;
					}
				}
			}
		}

		return this;
	},

	toggleClass: function( value, stateVal ) {
		var type = typeof value;

		if ( typeof stateVal === "boolean" && type === "string" ) {
			return stateVal ? this.addClass( value ) : this.removeClass( value );
		}

		if ( jQuery.isFunction( value ) ) {
			return this.each(function( i ) {
				jQuery( this ).toggleClass( value.call(this, i, this.className, stateVal), stateVal );
			});
		}

		return this.each(function() {
			if ( type === "string" ) {
				// toggle individual class names
				var className,
					i = 0,
					self = jQuery( this ),
					classNames = value.match( rnotwhite ) || [];

				while ( (className = classNames[ i++ ]) ) {
					// check each className given, space separated list
					if ( self.hasClass( className ) ) {
						self.removeClass( className );
					} else {
						self.addClass( className );
					}
				}

			// Toggle whole class name
			} else if ( type === strundefined || type === "boolean" ) {
				if ( this.className ) {
					// store className if set
					jQuery._data( this, "__className__", this.className );
				}

				// If the element has a class name or if we're passed "false",
				// then remove the whole classname (if there was one, the above saved it).
				// Otherwise bring back whatever was previously saved (if anything),
				// falling back to the empty string if nothing was stored.
				this.className = this.className || value === false ? "" : jQuery._data( this, "__className__" ) || "";
			}
		});
	},

	hasClass: function( selector ) {
		var className = " " + selector + " ",
			i = 0,
			l = this.length;
		for ( ; i < l; i++ ) {
			if ( this[i].nodeType === 1 && (" " + this[i].className + " ").replace(rclass, " ").indexOf( className ) >= 0 ) {
				return true;
			}
		}

		return false;
	}
});




// Return jQuery for attributes-only inclusion


jQuery.each( ("blur focus focusin focusout load resize scroll unload click dblclick " +
	"mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave " +
	"change select submit keydown keypress keyup error contextmenu").split(" "), function( i, name ) {

	// Handle event binding
	jQuery.fn[ name ] = function( data, fn ) {
		return arguments.length > 0 ?
			this.on( name, null, data, fn ) :
			this.trigger( name );
	};
});

jQuery.fn.extend({
	hover: function( fnOver, fnOut ) {
		return this.mouseenter( fnOver ).mouseleave( fnOut || fnOver );
	},

	bind: function( types, data, fn ) {
		return this.on( types, null, data, fn );
	},
	unbind: function( types, fn ) {
		return this.off( types, null, fn );
	},

	delegate: function( selector, types, data, fn ) {
		return this.on( types, selector, data, fn );
	},
	undelegate: function( selector, types, fn ) {
		// ( namespace ) or ( selector, types [, fn] )
		return arguments.length === 1 ? this.off( selector, "**" ) : this.off( types, selector || "**", fn );
	}
});


var nonce = jQuery.now();

var rquery = (/\?/);



var rvalidtokens = /(,)|(\[|{)|(}|])|"(?:[^"\\\r\n]|\\["\\\/bfnrt]|\\u[\da-fA-F]{4})*"\s*:?|true|false|null|-?(?!0\d)\d+(?:\.\d+|)(?:[eE][+-]?\d+|)/g;

jQuery.parseJSON = function( data ) {
	// Attempt to parse using the native JSON parser first
	if ( window.JSON && window.JSON.parse ) {
		// Support: Android 2.3
		// Workaround failure to string-cast null input
		return window.JSON.parse( data + "" );
	}

	var requireNonComma,
		depth = null,
		str = jQuery.trim( data + "" );

	// Guard against invalid (and possibly dangerous) input by ensuring that nothing remains
	// after removing valid tokens
	return str && !jQuery.trim( str.replace( rvalidtokens, function( token, comma, open, close ) {

		// Force termination if we see a misplaced comma
		if ( requireNonComma && comma ) {
			depth = 0;
		}

		// Perform no more replacements after returning to outermost depth
		if ( depth === 0 ) {
			return token;
		}

		// Commas must not follow "[", "{", or ","
		requireNonComma = open || comma;

		// Determine new depth
		// array/object open ("[" or "{"): depth += true - false (increment)
		// array/object close ("]" or "}"): depth += false - true (decrement)
		// other cases ("," or primitive): depth += true - true (numeric cast)
		depth += !close - !open;

		// Remove this token
		return "";
	}) ) ?
		( Function( "return " + str ) )() :
		jQuery.error( "Invalid JSON: " + data );
};


// Cross-browser xml parsing
jQuery.parseXML = function( data ) {
	var xml, tmp;
	if ( !data || typeof data !== "string" ) {
		return null;
	}
	try {
		if ( window.DOMParser ) { // Standard
			tmp = new DOMParser();
			xml = tmp.parseFromString( data, "text/xml" );
		} else { // IE
			xml = new ActiveXObject( "Microsoft.XMLDOM" );
			xml.async = "false";
			xml.loadXML( data );
		}
	} catch( e ) {
		xml = undefined;
	}
	if ( !xml || !xml.documentElement || xml.getElementsByTagName( "parsererror" ).length ) {
		jQuery.error( "Invalid XML: " + data );
	}
	return xml;
};


var
	// Document location
	ajaxLocParts,
	ajaxLocation,

	rhash = /#.*$/,
	rts = /([?&])_=[^&]*/,
	rheaders = /^(.*?):[ \t]*([^\r\n]*)\r?$/mg, // IE leaves an \r character at EOL
	// #7653, #8125, #8152: local protocol detection
	rlocalProtocol = /^(?:about|app|app-storage|.+-extension|file|res|widget):$/,
	rnoContent = /^(?:GET|HEAD)$/,
	rprotocol = /^\/\//,
	rurl = /^([\w.+-]+:)(?:\/\/(?:[^\/?#]*@|)([^\/?#:]*)(?::(\d+)|)|)/,

	/* Prefilters
	 * 1) They are useful to introduce custom dataTypes (see ajax/jsonp.js for an example)
	 * 2) These are called:
	 *    - BEFORE asking for a transport
	 *    - AFTER param serialization (s.data is a string if s.processData is true)
	 * 3) key is the dataType
	 * 4) the catchall symbol "*" can be used
	 * 5) execution will start with transport dataType and THEN continue down to "*" if needed
	 */
	prefilters = {},

	/* Transports bindings
	 * 1) key is the dataType
	 * 2) the catchall symbol "*" can be used
	 * 3) selection will start with transport dataType and THEN go to "*" if needed
	 */
	transports = {},

	// Avoid comment-prolog char sequence (#10098); must appease lint and evade compression
	allTypes = "*/".concat("*");

// #8138, IE may throw an exception when accessing
// a field from window.location if document.domain has been set
try {
	ajaxLocation = location.href;
} catch( e ) {
	// Use the href attribute of an A element
	// since IE will modify it given document.location
	ajaxLocation = document.createElement( "a" );
	ajaxLocation.href = "";
	ajaxLocation = ajaxLocation.href;
}

// Segment location into parts
ajaxLocParts = rurl.exec( ajaxLocation.toLowerCase() ) || [];

// Base "constructor" for jQuery.ajaxPrefilter and jQuery.ajaxTransport
function addToPrefiltersOrTransports( structure ) {

	// dataTypeExpression is optional and defaults to "*"
	return function( dataTypeExpression, func ) {

		if ( typeof dataTypeExpression !== "string" ) {
			func = dataTypeExpression;
			dataTypeExpression = "*";
		}

		var dataType,
			i = 0,
			dataTypes = dataTypeExpression.toLowerCase().match( rnotwhite ) || [];

		if ( jQuery.isFunction( func ) ) {
			// For each dataType in the dataTypeExpression
			while ( (dataType = dataTypes[i++]) ) {
				// Prepend if requested
				if ( dataType.charAt( 0 ) === "+" ) {
					dataType = dataType.slice( 1 ) || "*";
					(structure[ dataType ] = structure[ dataType ] || []).unshift( func );

				// Otherwise append
				} else {
					(structure[ dataType ] = structure[ dataType ] || []).push( func );
				}
			}
		}
	};
}

// Base inspection function for prefilters and transports
function inspectPrefiltersOrTransports( structure, options, originalOptions, jqXHR ) {

	var inspected = {},
		seekingTransport = ( structure === transports );

	function inspect( dataType ) {
		var selected;
		inspected[ dataType ] = true;
		jQuery.each( structure[ dataType ] || [], function( _, prefilterOrFactory ) {
			var dataTypeOrTransport = prefilterOrFactory( options, originalOptions, jqXHR );
			if ( typeof dataTypeOrTransport === "string" && !seekingTransport && !inspected[ dataTypeOrTransport ] ) {
				options.dataTypes.unshift( dataTypeOrTransport );
				inspect( dataTypeOrTransport );
				return false;
			} else if ( seekingTransport ) {
				return !( selected = dataTypeOrTransport );
			}
		});
		return selected;
	}

	return inspect( options.dataTypes[ 0 ] ) || !inspected[ "*" ] && inspect( "*" );
}

// A special extend for ajax options
// that takes "flat" options (not to be deep extended)
// Fixes #9887
function ajaxExtend( target, src ) {
	var deep, key,
		flatOptions = jQuery.ajaxSettings.flatOptions || {};

	for ( key in src ) {
		if ( src[ key ] !== undefined ) {
			( flatOptions[ key ] ? target : ( deep || (deep = {}) ) )[ key ] = src[ key ];
		}
	}
	if ( deep ) {
		jQuery.extend( true, target, deep );
	}

	return target;
}

/* Handles responses to an ajax request:
 * - finds the right dataType (mediates between content-type and expected dataType)
 * - returns the corresponding response
 */
function ajaxHandleResponses( s, jqXHR, responses ) {
	var firstDataType, ct, finalDataType, type,
		contents = s.contents,
		dataTypes = s.dataTypes;

	// Remove auto dataType and get content-type in the process
	while ( dataTypes[ 0 ] === "*" ) {
		dataTypes.shift();
		if ( ct === undefined ) {
			ct = s.mimeType || jqXHR.getResponseHeader("Content-Type");
		}
	}

	// Check if we're dealing with a known content-type
	if ( ct ) {
		for ( type in contents ) {
			if ( contents[ type ] && contents[ type ].test( ct ) ) {
				dataTypes.unshift( type );
				break;
			}
		}
	}

	// Check to see if we have a response for the expected dataType
	if ( dataTypes[ 0 ] in responses ) {
		finalDataType = dataTypes[ 0 ];
	} else {
		// Try convertible dataTypes
		for ( type in responses ) {
			if ( !dataTypes[ 0 ] || s.converters[ type + " " + dataTypes[0] ] ) {
				finalDataType = type;
				break;
			}
			if ( !firstDataType ) {
				firstDataType = type;
			}
		}
		// Or just use first one
		finalDataType = finalDataType || firstDataType;
	}

	// If we found a dataType
	// We add the dataType to the list if needed
	// and return the corresponding response
	if ( finalDataType ) {
		if ( finalDataType !== dataTypes[ 0 ] ) {
			dataTypes.unshift( finalDataType );
		}
		return responses[ finalDataType ];
	}
}

/* Chain conversions given the request and the original response
 * Also sets the responseXXX fields on the jqXHR instance
 */
function ajaxConvert( s, response, jqXHR, isSuccess ) {
	var conv2, current, conv, tmp, prev,
		converters = {},
		// Work with a copy of dataTypes in case we need to modify it for conversion
		dataTypes = s.dataTypes.slice();

	// Create converters map with lowercased keys
	if ( dataTypes[ 1 ] ) {
		for ( conv in s.converters ) {
			converters[ conv.toLowerCase() ] = s.converters[ conv ];
		}
	}

	current = dataTypes.shift();

	// Convert to each sequential dataType
	while ( current ) {

		if ( s.responseFields[ current ] ) {
			jqXHR[ s.responseFields[ current ] ] = response;
		}

		// Apply the dataFilter if provided
		if ( !prev && isSuccess && s.dataFilter ) {
			response = s.dataFilter( response, s.dataType );
		}

		prev = current;
		current = dataTypes.shift();

		if ( current ) {

			// There's only work to do if current dataType is non-auto
			if ( current === "*" ) {

				current = prev;

			// Convert response if prev dataType is non-auto and differs from current
			} else if ( prev !== "*" && prev !== current ) {

				// Seek a direct converter
				conv = converters[ prev + " " + current ] || converters[ "* " + current ];

				// If none found, seek a pair
				if ( !conv ) {
					for ( conv2 in converters ) {

						// If conv2 outputs current
						tmp = conv2.split( " " );
						if ( tmp[ 1 ] === current ) {

							// If prev can be converted to accepted input
							conv = converters[ prev + " " + tmp[ 0 ] ] ||
								converters[ "* " + tmp[ 0 ] ];
							if ( conv ) {
								// Condense equivalence converters
								if ( conv === true ) {
									conv = converters[ conv2 ];

								// Otherwise, insert the intermediate dataType
								} else if ( converters[ conv2 ] !== true ) {
									current = tmp[ 0 ];
									dataTypes.unshift( tmp[ 1 ] );
								}
								break;
							}
						}
					}
				}

				// Apply converter (if not an equivalence)
				if ( conv !== true ) {

					// Unless errors are allowed to bubble, catch and return them
					if ( conv && s[ "throws" ] ) {
						response = conv( response );
					} else {
						try {
							response = conv( response );
						} catch ( e ) {
							return { state: "parsererror", error: conv ? e : "No conversion from " + prev + " to " + current };
						}
					}
				}
			}
		}
	}

	return { state: "success", data: response };
}

jQuery.extend({

	// Counter for holding the number of active queries
	active: 0,

	// Last-Modified header cache for next request
	lastModified: {},
	etag: {},

	ajaxSettings: {
		url: ajaxLocation,
		type: "GET",
		isLocal: rlocalProtocol.test( ajaxLocParts[ 1 ] ),
		global: true,
		processData: true,
		async: true,
		contentType: "application/x-www-form-urlencoded; charset=UTF-8",
		/*
		timeout: 0,
		data: null,
		dataType: null,
		username: null,
		password: null,
		cache: null,
		throws: false,
		traditional: false,
		headers: {},
		*/

		accepts: {
			"*": allTypes,
			text: "text/plain",
			html: "text/html",
			xml: "application/xml, text/xml",
			json: "application/json, text/javascript"
		},

		contents: {
			xml: /xml/,
			html: /html/,
			json: /json/
		},

		responseFields: {
			xml: "responseXML",
			text: "responseText",
			json: "responseJSON"
		},

		// Data converters
		// Keys separate source (or catchall "*") and destination types with a single space
		converters: {

			// Convert anything to text
			"* text": String,

			// Text to html (true = no transformation)
			"text html": true,

			// Evaluate text as a json expression
			"text json": jQuery.parseJSON,

			// Parse text as xml
			"text xml": jQuery.parseXML
		},

		// For options that shouldn't be deep extended:
		// you can add your own custom options here if
		// and when you create one that shouldn't be
		// deep extended (see ajaxExtend)
		flatOptions: {
			url: true,
			context: true
		}
	},

	// Creates a full fledged settings object into target
	// with both ajaxSettings and settings fields.
	// If target is omitted, writes into ajaxSettings.
	ajaxSetup: function( target, settings ) {
		return settings ?

			// Building a settings object
			ajaxExtend( ajaxExtend( target, jQuery.ajaxSettings ), settings ) :

			// Extending ajaxSettings
			ajaxExtend( jQuery.ajaxSettings, target );
	},

	ajaxPrefilter: addToPrefiltersOrTransports( prefilters ),
	ajaxTransport: addToPrefiltersOrTransports( transports ),

	// Main method
	ajax: function( url, options ) {

		// If url is an object, simulate pre-1.5 signature
		if ( typeof url === "object" ) {
			options = url;
			url = undefined;
		}

		// Force options to be an object
		options = options || {};

		var // Cross-domain detection vars
			parts,
			// Loop variable
			i,
			// URL without anti-cache param
			cacheURL,
			// Response headers as string
			responseHeadersString,
			// timeout handle
			timeoutTimer,

			// To know if global events are to be dispatched
			fireGlobals,

			transport,
			// Response headers
			responseHeaders,
			// Create the final options object
			s = jQuery.ajaxSetup( {}, options ),
			// Callbacks context
			callbackContext = s.context || s,
			// Context for global events is callbackContext if it is a DOM node or jQuery collection
			globalEventContext = s.context && ( callbackContext.nodeType || callbackContext.jquery ) ?
				jQuery( callbackContext ) :
				jQuery.event,
			// Deferreds
			deferred = jQuery.Deferred(),
			completeDeferred = jQuery.Callbacks("once memory"),
			// Status-dependent callbacks
			statusCode = s.statusCode || {},
			// Headers (they are sent all at once)
			requestHeaders = {},
			requestHeadersNames = {},
			// The jqXHR state
			state = 0,
			// Default abort message
			strAbort = "canceled",
			// Fake xhr
			jqXHR = {
				readyState: 0,

				// Builds headers hashtable if needed
				getResponseHeader: function( key ) {
					var match;
					if ( state === 2 ) {
						if ( !responseHeaders ) {
							responseHeaders = {};
							while ( (match = rheaders.exec( responseHeadersString )) ) {
								responseHeaders[ match[1].toLowerCase() ] = match[ 2 ];
							}
						}
						match = responseHeaders[ key.toLowerCase() ];
					}
					return match == null ? null : match;
				},

				// Raw string
				getAllResponseHeaders: function() {
					return state === 2 ? responseHeadersString : null;
				},

				// Caches the header
				setRequestHeader: function( name, value ) {
					var lname = name.toLowerCase();
					if ( !state ) {
						name = requestHeadersNames[ lname ] = requestHeadersNames[ lname ] || name;
						requestHeaders[ name ] = value;
					}
					return this;
				},

				// Overrides response content-type header
				overrideMimeType: function( type ) {
					if ( !state ) {
						s.mimeType = type;
					}
					return this;
				},

				// Status-dependent callbacks
				statusCode: function( map ) {
					var code;
					if ( map ) {
						if ( state < 2 ) {
							for ( code in map ) {
								// Lazy-add the new callback in a way that preserves old ones
								statusCode[ code ] = [ statusCode[ code ], map[ code ] ];
							}
						} else {
							// Execute the appropriate callbacks
							jqXHR.always( map[ jqXHR.status ] );
						}
					}
					return this;
				},

				// Cancel the request
				abort: function( statusText ) {
					var finalText = statusText || strAbort;
					if ( transport ) {
						transport.abort( finalText );
					}
					done( 0, finalText );
					return this;
				}
			};

		// Attach deferreds
		deferred.promise( jqXHR ).complete = completeDeferred.add;
		jqXHR.success = jqXHR.done;
		jqXHR.error = jqXHR.fail;

		// Remove hash character (#7531: and string promotion)
		// Add protocol if not provided (#5866: IE7 issue with protocol-less urls)
		// Handle falsy url in the settings object (#10093: consistency with old signature)
		// We also use the url parameter if available
		s.url = ( ( url || s.url || ajaxLocation ) + "" ).replace( rhash, "" ).replace( rprotocol, ajaxLocParts[ 1 ] + "//" );

		// Alias method option to type as per ticket #12004
		s.type = options.method || options.type || s.method || s.type;

		// Extract dataTypes list
		s.dataTypes = jQuery.trim( s.dataType || "*" ).toLowerCase().match( rnotwhite ) || [ "" ];

		// A cross-domain request is in order when we have a protocol:host:port mismatch
		if ( s.crossDomain == null ) {
			parts = rurl.exec( s.url.toLowerCase() );
			s.crossDomain = !!( parts &&
				( parts[ 1 ] !== ajaxLocParts[ 1 ] || parts[ 2 ] !== ajaxLocParts[ 2 ] ||
					( parts[ 3 ] || ( parts[ 1 ] === "http:" ? "80" : "443" ) ) !==
						( ajaxLocParts[ 3 ] || ( ajaxLocParts[ 1 ] === "http:" ? "80" : "443" ) ) )
			);
		}

		// Convert data if not already a string
		if ( s.data && s.processData && typeof s.data !== "string" ) {
			s.data = jQuery.param( s.data, s.traditional );
		}

		// Apply prefilters
		inspectPrefiltersOrTransports( prefilters, s, options, jqXHR );

		// If request was aborted inside a prefilter, stop there
		if ( state === 2 ) {
			return jqXHR;
		}

		// We can fire global events as of now if asked to
		fireGlobals = s.global;

		// Watch for a new set of requests
		if ( fireGlobals && jQuery.active++ === 0 ) {
			jQuery.event.trigger("ajaxStart");
		}

		// Uppercase the type
		s.type = s.type.toUpperCase();

		// Determine if request has content
		s.hasContent = !rnoContent.test( s.type );

		// Save the URL in case we're toying with the If-Modified-Since
		// and/or If-None-Match header later on
		cacheURL = s.url;

		// More options handling for requests with no content
		if ( !s.hasContent ) {

			// If data is available, append data to url
			if ( s.data ) {
				cacheURL = ( s.url += ( rquery.test( cacheURL ) ? "&" : "?" ) + s.data );
				// #9682: remove data so that it's not used in an eventual retry
				delete s.data;
			}

			// Add anti-cache in url if needed
			if ( s.cache === false ) {
				s.url = rts.test( cacheURL ) ?

					// If there is already a '_' parameter, set its value
					cacheURL.replace( rts, "$1_=" + nonce++ ) :

					// Otherwise add one to the end
					cacheURL + ( rquery.test( cacheURL ) ? "&" : "?" ) + "_=" + nonce++;
			}
		}

		// Set the If-Modified-Since and/or If-None-Match header, if in ifModified mode.
		if ( s.ifModified ) {
			if ( jQuery.lastModified[ cacheURL ] ) {
				jqXHR.setRequestHeader( "If-Modified-Since", jQuery.lastModified[ cacheURL ] );
			}
			if ( jQuery.etag[ cacheURL ] ) {
				jqXHR.setRequestHeader( "If-None-Match", jQuery.etag[ cacheURL ] );
			}
		}

		// Set the correct header, if data is being sent
		if ( s.data && s.hasContent && s.contentType !== false || options.contentType ) {
			jqXHR.setRequestHeader( "Content-Type", s.contentType );
		}

		// Set the Accepts header for the server, depending on the dataType
		jqXHR.setRequestHeader(
			"Accept",
			s.dataTypes[ 0 ] && s.accepts[ s.dataTypes[0] ] ?
				s.accepts[ s.dataTypes[0] ] + ( s.dataTypes[ 0 ] !== "*" ? ", " + allTypes + "; q=0.01" : "" ) :
				s.accepts[ "*" ]
		);

		// Check for headers option
		for ( i in s.headers ) {
			jqXHR.setRequestHeader( i, s.headers[ i ] );
		}

		// Allow custom headers/mimetypes and early abort
		if ( s.beforeSend && ( s.beforeSend.call( callbackContext, jqXHR, s ) === false || state === 2 ) ) {
			// Abort if not done already and return
			return jqXHR.abort();
		}

		// aborting is no longer a cancellation
		strAbort = "abort";

		// Install callbacks on deferreds
		for ( i in { success: 1, error: 1, complete: 1 } ) {
			jqXHR[ i ]( s[ i ] );
		}

		// Get transport
		transport = inspectPrefiltersOrTransports( transports, s, options, jqXHR );

		// If no transport, we auto-abort
		if ( !transport ) {
			done( -1, "No Transport" );
		} else {
			jqXHR.readyState = 1;

			// Send global event
			if ( fireGlobals ) {
				globalEventContext.trigger( "ajaxSend", [ jqXHR, s ] );
			}
			// Timeout
			if ( s.async && s.timeout > 0 ) {
				timeoutTimer = setTimeout(function() {
					jqXHR.abort("timeout");
				}, s.timeout );
			}

			try {
				state = 1;
				transport.send( requestHeaders, done );
			} catch ( e ) {
				// Propagate exception as error if not done
				if ( state < 2 ) {
					done( -1, e );
				// Simply rethrow otherwise
				} else {
					throw e;
				}
			}
		}

		// Callback for when everything is done
		function done( status, nativeStatusText, responses, headers ) {
			var isSuccess, success, error, response, modified,
				statusText = nativeStatusText;

			// Called once
			if ( state === 2 ) {
				return;
			}

			// State is "done" now
			state = 2;

			// Clear timeout if it exists
			if ( timeoutTimer ) {
				clearTimeout( timeoutTimer );
			}

			// Dereference transport for early garbage collection
			// (no matter how long the jqXHR object will be used)
			transport = undefined;

			// Cache response headers
			responseHeadersString = headers || "";

			// Set readyState
			jqXHR.readyState = status > 0 ? 4 : 0;

			// Determine if successful
			isSuccess = status >= 200 && status < 300 || status === 304;

			// Get response data
			if ( responses ) {
				response = ajaxHandleResponses( s, jqXHR, responses );
			}

			// Convert no matter what (that way responseXXX fields are always set)
			response = ajaxConvert( s, response, jqXHR, isSuccess );

			// If successful, handle type chaining
			if ( isSuccess ) {

				// Set the If-Modified-Since and/or If-None-Match header, if in ifModified mode.
				if ( s.ifModified ) {
					modified = jqXHR.getResponseHeader("Last-Modified");
					if ( modified ) {
						jQuery.lastModified[ cacheURL ] = modified;
					}
					modified = jqXHR.getResponseHeader("etag");
					if ( modified ) {
						jQuery.etag[ cacheURL ] = modified;
					}
				}

				// if no content
				if ( status === 204 || s.type === "HEAD" ) {
					statusText = "nocontent";

				// if not modified
				} else if ( status === 304 ) {
					statusText = "notmodified";

				// If we have data, let's convert it
				} else {
					statusText = response.state;
					success = response.data;
					error = response.error;
					isSuccess = !error;
				}
			} else {
				// We extract error from statusText
				// then normalize statusText and status for non-aborts
				error = statusText;
				if ( status || !statusText ) {
					statusText = "error";
					if ( status < 0 ) {
						status = 0;
					}
				}
			}

			// Set data for the fake xhr object
			jqXHR.status = status;
			jqXHR.statusText = ( nativeStatusText || statusText ) + "";

			// Success/Error
			if ( isSuccess ) {
				deferred.resolveWith( callbackContext, [ success, statusText, jqXHR ] );
			} else {
				deferred.rejectWith( callbackContext, [ jqXHR, statusText, error ] );
			}

			// Status-dependent callbacks
			jqXHR.statusCode( statusCode );
			statusCode = undefined;

			if ( fireGlobals ) {
				globalEventContext.trigger( isSuccess ? "ajaxSuccess" : "ajaxError",
					[ jqXHR, s, isSuccess ? success : error ] );
			}

			// Complete
			completeDeferred.fireWith( callbackContext, [ jqXHR, statusText ] );

			if ( fireGlobals ) {
				globalEventContext.trigger( "ajaxComplete", [ jqXHR, s ] );
				// Handle the global AJAX counter
				if ( !( --jQuery.active ) ) {
					jQuery.event.trigger("ajaxStop");
				}
			}
		}

		return jqXHR;
	},

	getJSON: function( url, data, callback ) {
		return jQuery.get( url, data, callback, "json" );
	},

	getScript: function( url, callback ) {
		return jQuery.get( url, undefined, callback, "script" );
	}
});

jQuery.each( [ "get", "post" ], function( i, method ) {
	jQuery[ method ] = function( url, data, callback, type ) {
		// shift arguments if data argument was omitted
		if ( jQuery.isFunction( data ) ) {
			type = type || callback;
			callback = data;
			data = undefined;
		}

		return jQuery.ajax({
			url: url,
			type: method,
			dataType: type,
			data: data,
			success: callback
		});
	};
});

// Attach a bunch of functions for handling common AJAX events
jQuery.each( [ "ajaxStart", "ajaxStop", "ajaxComplete", "ajaxError", "ajaxSuccess", "ajaxSend" ], function( i, type ) {
	jQuery.fn[ type ] = function( fn ) {
		return this.on( type, fn );
	};
});


jQuery._evalUrl = function( url ) {
	return jQuery.ajax({
		url: url,
		type: "GET",
		dataType: "script",
		async: false,
		global: false,
		"throws": true
	});
};


jQuery.fn.extend({
	wrapAll: function( html ) {
		if ( jQuery.isFunction( html ) ) {
			return this.each(function(i) {
				jQuery(this).wrapAll( html.call(this, i) );
			});
		}

		if ( this[0] ) {
			// The elements to wrap the target around
			var wrap = jQuery( html, this[0].ownerDocument ).eq(0).clone(true);

			if ( this[0].parentNode ) {
				wrap.insertBefore( this[0] );
			}

			wrap.map(function() {
				var elem = this;

				while ( elem.firstChild && elem.firstChild.nodeType === 1 ) {
					elem = elem.firstChild;
				}

				return elem;
			}).append( this );
		}

		return this;
	},

	wrapInner: function( html ) {
		if ( jQuery.isFunction( html ) ) {
			return this.each(function(i) {
				jQuery(this).wrapInner( html.call(this, i) );
			});
		}

		return this.each(function() {
			var self = jQuery( this ),
				contents = self.contents();

			if ( contents.length ) {
				contents.wrapAll( html );

			} else {
				self.append( html );
			}
		});
	},

	wrap: function( html ) {
		var isFunction = jQuery.isFunction( html );

		return this.each(function(i) {
			jQuery( this ).wrapAll( isFunction ? html.call(this, i) : html );
		});
	},

	unwrap: function() {
		return this.parent().each(function() {
			if ( !jQuery.nodeName( this, "body" ) ) {
				jQuery( this ).replaceWith( this.childNodes );
			}
		}).end();
	}
});


jQuery.expr.filters.hidden = function( elem ) {
	// Support: Opera <= 12.12
	// Opera reports offsetWidths and offsetHeights less than zero on some elements
	return elem.offsetWidth <= 0 && elem.offsetHeight <= 0 ||
		(!support.reliableHiddenOffsets() &&
			((elem.style && elem.style.display) || jQuery.css( elem, "display" )) === "none");
};

jQuery.expr.filters.visible = function( elem ) {
	return !jQuery.expr.filters.hidden( elem );
};




var r20 = /%20/g,
	rbracket = /\[\]$/,
	rCRLF = /\r?\n/g,
	rsubmitterTypes = /^(?:submit|button|image|reset|file)$/i,
	rsubmittable = /^(?:input|select|textarea|keygen)/i;

function buildParams( prefix, obj, traditional, add ) {
	var name;

	if ( jQuery.isArray( obj ) ) {
		// Serialize array item.
		jQuery.each( obj, function( i, v ) {
			if ( traditional || rbracket.test( prefix ) ) {
				// Treat each array item as a scalar.
				add( prefix, v );

			} else {
				// Item is non-scalar (array or object), encode its numeric index.
				buildParams( prefix + "[" + ( typeof v === "object" ? i : "" ) + "]", v, traditional, add );
			}
		});

	} else if ( !traditional && jQuery.type( obj ) === "object" ) {
		// Serialize object item.
		for ( name in obj ) {
			buildParams( prefix + "[" + name + "]", obj[ name ], traditional, add );
		}

	} else {
		// Serialize scalar item.
		add( prefix, obj );
	}
}

// Serialize an array of form elements or a set of
// key/values into a query string
jQuery.param = function( a, traditional ) {
	var prefix,
		s = [],
		add = function( key, value ) {
			// If value is a function, invoke it and return its value
			value = jQuery.isFunction( value ) ? value() : ( value == null ? "" : value );
			s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
		};

	// Set traditional to true for jQuery <= 1.3.2 behavior.
	if ( traditional === undefined ) {
		traditional = jQuery.ajaxSettings && jQuery.ajaxSettings.traditional;
	}

	// If an array was passed in, assume that it is an array of form elements.
	if ( jQuery.isArray( a ) || ( a.jquery && !jQuery.isPlainObject( a ) ) ) {
		// Serialize the form elements
		jQuery.each( a, function() {
			add( this.name, this.value );
		});

	} else {
		// If traditional, encode the "old" way (the way 1.3.2 or older
		// did it), otherwise encode params recursively.
		for ( prefix in a ) {
			buildParams( prefix, a[ prefix ], traditional, add );
		}
	}

	// Return the resulting serialization
	return s.join( "&" ).replace( r20, "+" );
};

jQuery.fn.extend({
	serialize: function() {
		return jQuery.param( this.serializeArray() );
	},
	serializeArray: function() {
		return this.map(function() {
			// Can add propHook for "elements" to filter or add form elements
			var elements = jQuery.prop( this, "elements" );
			return elements ? jQuery.makeArray( elements ) : this;
		})
		.filter(function() {
			var type = this.type;
			// Use .is(":disabled") so that fieldset[disabled] works
			return this.name && !jQuery( this ).is( ":disabled" ) &&
				rsubmittable.test( this.nodeName ) && !rsubmitterTypes.test( type ) &&
				( this.checked || !rcheckableType.test( type ) );
		})
		.map(function( i, elem ) {
			var val = jQuery( this ).val();

			return val == null ?
				null :
				jQuery.isArray( val ) ?
					jQuery.map( val, function( val ) {
						return { name: elem.name, value: val.replace( rCRLF, "\r\n" ) };
					}) :
					{ name: elem.name, value: val.replace( rCRLF, "\r\n" ) };
		}).get();
	}
});


// Create the request object
// (This is still attached to ajaxSettings for backward compatibility)
jQuery.ajaxSettings.xhr = window.ActiveXObject !== undefined ?
	// Support: IE6+
	function() {

		// XHR cannot access local files, always use ActiveX for that case
		return !this.isLocal &&

			// Support: IE7-8
			// oldIE XHR does not support non-RFC2616 methods (#13240)
			// See http://msdn.microsoft.com/en-us/library/ie/ms536648(v=vs.85).aspx
			// and http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9
			// Although this check for six methods instead of eight
			// since IE also does not support "trace" and "connect"
			/^(get|post|head|put|delete|options)$/i.test( this.type ) &&

			createStandardXHR() || createActiveXHR();
	} :
	// For all other browsers, use the standard XMLHttpRequest object
	createStandardXHR;

//bh

	function createXHR(isMSIE) {
		try {
			return (isMSIE ? new window.ActiveXObject( "Microsoft.XMLHTTP" ) : new window.XMLHttpRequest());
		} catch( e ) {}
	}

 jQuery.ajaxSettings.xhr = (window.ActiveXObject === undefined ? createXHR :  
	function() {
		return (this.url == document.location || this.url.indexOf("http") == 0 || !this.isLocal) &&  // BH MSIE fix
			/^(get|post|head|put|delete|options)$/i.test( this.type ) &&
			createXHR() || createXHR(1);
	});
 
//bh


var xhrId = 0,
	xhrCallbacks = {},
	xhrSupported = jQuery.ajaxSettings.xhr();

// Support: IE<10
// Open requests must be manually aborted on unload (#5280)
if ( window.ActiveXObject ) {
	jQuery( window ).on( "unload", function() {
		for ( var key in xhrCallbacks ) {
			xhrCallbacks[ key ]( undefined, true );
		}
	});
}

// Determine support properties
support.cors = !!xhrSupported && ( "withCredentials" in xhrSupported );
xhrSupported = support.ajax = !!xhrSupported;

// Create transport if the browser can provide an xhr
if ( xhrSupported ) {

	jQuery.ajaxTransport(function( options ) {
		// Cross domain only allowed if supported through XMLHttpRequest
		if ( !options.crossDomain || support.cors ) {

			var callback;

			return {
				send: function( headers, complete ) {
					var i,
						xhr = options.xhr(),
						id = ++xhrId;

					// Open the socket
					// BH 2018 quiet option
					self.Clazz&&Clazz._quiet || console.log("xhr.open async=" + options.async + " url=" + options.url);
					xhr.open( options.type, options.url, options.async, options.username, options.password );

					// Apply custom fields if provided
					if ( options.xhrFields ) {
						for ( i in options.xhrFields ) {
							xhr[ i ] = options.xhrFields[ i ];
						}
					}

					// Override mime type if needed
					if ( options.mimeType && xhr.overrideMimeType ) {
						xhr.overrideMimeType( options.mimeType );
					}

					// X-Requested-With header
					// For cross-domain requests, seeing as conditions for a preflight are
					// akin to a jigsaw puzzle, we simply never set it to be sure.
					// (it can always be set on a per-request basis or even using ajaxSetup)
					// For same-domain requests, won't change header if already provided.
					if ( !options.crossDomain && !headers["X-Requested-With"] ) {
						headers["X-Requested-With"] = "XMLHttpRequest";
					}

					// Set headers
					for ( i in headers ) {
						// Support: IE<9
						// IE's ActiveXObject throws a 'Type Mismatch' exception when setting
						// request header to a null-value.
						//
						// To keep consistent with other XHR implementations, cast the value
						// to string and ignore `undefined`.
						if ( headers[ i ] !== undefined ) {
							xhr.setRequestHeader( i, headers[ i ] + "" );
						}
					}

					// Do send the request
					// This may raise an exception which is actually
					// handled in jQuery.ajax (so no try/catch here)
					xhr.send( ( options.hasContent && options.data ) || null );

					// Listener
					callback = function( _, isAbort ) {
						var status, statusText, responses;

						// Was never called and is aborted or complete
						if ( callback && ( isAbort || xhr.readyState === 4 ) ) {
							// Clean up
							delete xhrCallbacks[ id ];
							callback = undefined;
							xhr.onreadystatechange = jQuery.noop;

							// Abort manually if needed
							if ( isAbort ) {
								if ( xhr.readyState !== 4 ) {
									xhr.abort();
								}
							} else {
								responses = {};
								status = xhr.status;

								// Support: IE<10
								// Accessing binary-data responseText throws an exception
								// (#11426)
								if ( typeof xhr.responseText === "string" ) {
									responses.text = xhr.responseText;
								}

								// Firefox throws an exception when accessing
								// statusText for faulty cross-domain requests
								try {
									statusText = xhr.statusText;
								} catch( e ) {
									// We normalize with Webkit giving an empty statusText
									statusText = "";
								}

								// Filter status for non standard behaviors

								// If the request is local and we have data: assume a success
								// (success with no data won't get notified, that's the best we
								// can do given current implementations)
								if ( !status && options.isLocal && !options.crossDomain ) {
									status = responses.text ? 200 : 404;
								// IE - #1450: sometimes returns 1223 when it should be 204
								} else if ( status === 1223 ) {
									status = 204;
								}
							}
						}

						// Call complete if needed
						if ( responses ) {
							complete( status, statusText, responses, xhr.getAllResponseHeaders() );
						}
					};

					if ( !options.async ) {
						// if we're in sync mode we fire the callback
						callback();
					} else if ( xhr.readyState === 4 ) {
						// (IE6 & IE7) if it's in cache and has been
						// retrieved directly we need to fire the callback
						setTimeout( callback );
					} else {
						// Add to the list of active xhr callbacks
						xhr.onreadystatechange = xhrCallbacks[ id ] = callback;
					}
				},

				abort: function() {
					if ( callback ) {
						callback( undefined, true );
					}
				}
			};
		}
	});
}

// Functions to create xhrs
function createStandardXHR() {
	try {
		return new window.XMLHttpRequest();
	} catch( e ) {}
}

function createActiveXHR() {
	try {
		return new window.ActiveXObject( "Microsoft.XMLHTTP" );
	} catch( e ) {}
}




// Install script dataType
jQuery.ajaxSetup({
	accepts: {
		script: "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"
	},
	contents: {
		script: /(?:java|ecma)script/
	},
	converters: {
		"text script": function( text ) {
			jQuery.globalEval( text );
			return text;
		}
	}
});

// Handle cache's special case and global
jQuery.ajaxPrefilter( "script", function( s ) {
	if ( s.cache === undefined ) {
		s.cache = false;
	}
	if ( s.crossDomain ) {
		s.type = "GET";
		s.global = false;
	}
});

// Bind script tag hack transport
jQuery.ajaxTransport( "script", function(s) {

	// This transport only deals with cross domain requests
	if ( s.crossDomain ) {

		var script,
			head = document.head || jQuery("head")[0] || document.documentElement;

		return {

			send: function( _, callback ) {

				script = document.createElement("script");

				script.async = true;

				if ( s.scriptCharset ) {
					script.charset = s.scriptCharset;
				}

				script.src = s.url;

				// Attach handlers for all browsers
				script.onload = script.onreadystatechange = function( _, isAbort ) {

					if ( isAbort || !script.readyState || /loaded|complete/.test( script.readyState ) ) {

						// Handle memory leak in IE
						script.onload = script.onreadystatechange = null;

						// Remove the script
						if ( script.parentNode ) {
							script.parentNode.removeChild( script );
						}

						// Dereference the script
						script = null;

						// Callback if not abort
						if ( !isAbort ) {
							callback( 200, "success" );
						}
					}
				};

				// Circumvent IE6 bugs with base elements (#2709 and #4378) by prepending
				// Use native DOM manipulation to avoid our domManip AJAX trickery
				head.insertBefore( script, head.firstChild );
			},

			abort: function() {
				if ( script ) {
					script.onload( undefined, true );
				}
			}
		};
	}
});




var oldCallbacks = [],
	rjsonp = /(=)\?(?=&|$)|\?\?/;

// Default jsonp settings
jQuery.ajaxSetup({
	jsonp: "callback",
	jsonpCallback: function() {
		var callback = oldCallbacks.pop() || ( jQuery.expando + "_" + ( nonce++ ) );
		this[ callback ] = true;
		return callback;
	}
});

// Detect, normalize options and install callbacks for jsonp requests
jQuery.ajaxPrefilter( "json jsonp", function( s, originalSettings, jqXHR ) {

	var callbackName, overwritten, responseContainer,
		jsonProp = s.jsonp !== false && ( rjsonp.test( s.url ) ?
			"url" :
			typeof s.data === "string" && !( s.contentType || "" ).indexOf("application/x-www-form-urlencoded") && rjsonp.test( s.data ) && "data"
		);

	// Handle iff the expected data type is "jsonp" or we have a parameter to set
	if ( jsonProp || s.dataTypes[ 0 ] === "jsonp" ) {

		// Get callback name, remembering preexisting value associated with it
		callbackName = s.jsonpCallback = jQuery.isFunction( s.jsonpCallback ) ?
			s.jsonpCallback() :
			s.jsonpCallback;

		// Insert callback into url or form data
		if ( jsonProp ) {
			s[ jsonProp ] = s[ jsonProp ].replace( rjsonp, "$1" + callbackName );
		} else if ( s.jsonp !== false ) {
			s.url += ( rquery.test( s.url ) ? "&" : "?" ) + s.jsonp + "=" + callbackName;
		}

		// Use data converter to retrieve json after script execution
		s.converters["script json"] = function() {
			if ( !responseContainer ) {
				jQuery.error( callbackName + " was not called" );
			}
			return responseContainer[ 0 ];
		};

		// force json dataType
		s.dataTypes[ 0 ] = "json";

		// Install callback
		overwritten = window[ callbackName ];
		window[ callbackName ] = function() {
			responseContainer = arguments;
		};

		// Clean-up function (fires after converters)
		jqXHR.always(function() {
			// Restore preexisting value
			window[ callbackName ] = overwritten;

			// Save back as free
			if ( s[ callbackName ] ) {
				// make sure that re-using the options doesn't screw things around
				s.jsonpCallback = originalSettings.jsonpCallback;

				// save the callback name for future use
				oldCallbacks.push( callbackName );
			}

			// Call if it was a function and we have a response
			if ( responseContainer && jQuery.isFunction( overwritten ) ) {
				overwritten( responseContainer[ 0 ] );
			}

			responseContainer = overwritten = undefined;
		});

		// Delegate to script
		return "script";
	}
});




// data: string of html
// context (optional): If specified, the fragment will be created in this context, defaults to document
// keepScripts (optional): If true, will include scripts passed in the html string
jQuery.parseHTML = function( data, context, keepScripts ) {
	if ( !data || typeof data !== "string" ) {
		return null;
	}
	if ( typeof context === "boolean" ) {
		keepScripts = context;
		context = false;
	}
	context = context || document;

	var parsed = rsingleTag.exec( data ),
		scripts = !keepScripts && [];

	// Single tag
	if ( parsed ) {
		return [ context.createElement( parsed[1] ) ];
	}

	parsed = jQuery.buildFragment( [ data ], context, scripts );

	if ( scripts && scripts.length ) {
		jQuery( scripts ).remove();
	}

	return jQuery.merge( [], parsed.childNodes );
};


// Keep a copy of the old load method
var _load = jQuery.fn.load;

/**
 * Load a url into a page
 */
jQuery.fn.load = function( url, params, callback ) {
	if ( typeof url !== "string" && _load ) {
		return _load.apply( this, arguments );
	}

	var selector, response, type,
		self = this,
		off = url.indexOf(" ");

	if ( off >= 0 ) {
		selector = url.slice( off, url.length );
		url = url.slice( 0, off );
	}

	// If it's a function
	if ( jQuery.isFunction( params ) ) {

		// We assume that it's the callback
		callback = params;
		params = undefined;

	// Otherwise, build a param string
	} else if ( params && typeof params === "object" ) {
		type = "POST";
	}

	// If we have elements to modify, make the request
	if ( self.length > 0 ) {
		jQuery.ajax({
			url: url,

			// if "type" variable is undefined, then "GET" method will be used
			type: type,
			dataType: "html",
			data: params
		}).done(function( responseText ) {

			// Save response for use in complete callback
			response = arguments;

			self.html( selector ?

				// If a selector was specified, locate the right elements in a dummy div
				// Exclude scripts to avoid IE 'Permission Denied' errors
				jQuery("<div>").append( jQuery.parseHTML( responseText ) ).find( selector ) :

				// Otherwise use the full result
				responseText );

		}).complete( callback && function( jqXHR, status ) {
			self.each( callback, response || [ jqXHR.responseText, status, jqXHR ] );
		});
	}

	return this;
};




jQuery.expr.filters.animated = function( elem ) {
	return jQuery.grep(jQuery.timers, function( fn ) {
		return elem === fn.elem;
	}).length;
};





var docElem = window.document.documentElement;

/**
 * Gets a window from an element
 */
function getWindow( elem ) {
	return jQuery.isWindow( elem ) ?
		elem :
		elem.nodeType === 9 ?
			elem.defaultView || elem.parentWindow :
			false;
}

jQuery.offset = {
	setOffset: function( elem, options, i ) {
		var curPosition, curLeft, curCSSTop, curTop, curOffset, curCSSLeft, calculatePosition,
			position = jQuery.css( elem, "position" ),
			curElem = jQuery( elem ),
			props = {};

		// set position first, in-case top/left are set even on static elem
		if ( position === "static" ) {
			elem.style.position = "relative";
		}

		curOffset = curElem.offset();
		curCSSTop = jQuery.css( elem, "top" );
		curCSSLeft = jQuery.css( elem, "left" );
		calculatePosition = ( position === "absolute" || position === "fixed" ) &&
			jQuery.inArray("auto", [ curCSSTop, curCSSLeft ] ) > -1;

		// need to be able to calculate position if either top or left is auto and position is either absolute or fixed
		if ( calculatePosition ) {
			curPosition = curElem.position();
			curTop = curPosition.top;
			curLeft = curPosition.left;
		} else {
			curTop = parseFloat( curCSSTop ) || 0;
			curLeft = parseFloat( curCSSLeft ) || 0;
		}

		if ( jQuery.isFunction( options ) ) {
			options = options.call( elem, i, curOffset );
		}

		if ( options.top != null ) {
			props.top = ( options.top - curOffset.top ) + curTop;
		}
		if ( options.left != null ) {
			props.left = ( options.left - curOffset.left ) + curLeft;
		}

		if ( "using" in options ) {
			options.using.call( elem, props );
		} else {
			curElem.css( props );
		}
	}
};

jQuery.fn.extend({
	offset: function( options ) {
		if ( arguments.length ) {
			return options === undefined ?
				this :
				this.each(function( i ) {
					jQuery.offset.setOffset( this, options, i );
				});
		}

		var docElem, win,
			box = { top: 0, left: 0 },
			elem = this[ 0 ],
			doc = elem && elem.ownerDocument;

		if ( !doc ) {
			return;
		}

		docElem = doc.documentElement;

		// Make sure it's not a disconnected DOM node
		if ( !jQuery.contains( docElem, elem ) ) {
			return box;
		}

		// If we don't have gBCR, just use 0,0 rather than error
		// BlackBerry 5, iOS 3 (original iPhone)
		if ( typeof elem.getBoundingClientRect !== strundefined ) {
			box = elem.getBoundingClientRect();
		}
		win = getWindow( doc );
		return {
			top: box.top  + ( win.pageYOffset || docElem.scrollTop )  - ( docElem.clientTop  || 0 ),
			left: box.left + ( win.pageXOffset || docElem.scrollLeft ) - ( docElem.clientLeft || 0 )
		};
	},

	position: function() {
		if ( !this[ 0 ] ) {
			return;
		}

		var offsetParent, offset,
			parentOffset = { top: 0, left: 0 },
			elem = this[ 0 ];

		// fixed elements are offset from window (parentOffset = {top:0, left: 0}, because it is its only offset parent
		if ( jQuery.css( elem, "position" ) === "fixed" ) {
			// we assume that getBoundingClientRect is available when computed position is fixed
			offset = elem.getBoundingClientRect();
		} else {
			// Get *real* offsetParent
			offsetParent = this.offsetParent();

			// Get correct offsets
			offset = this.offset();
			if ( !jQuery.nodeName( offsetParent[ 0 ], "html" ) ) {
				parentOffset = offsetParent.offset();
			}

			// Add offsetParent borders
			parentOffset.top  += jQuery.css( offsetParent[ 0 ], "borderTopWidth", true );
			parentOffset.left += jQuery.css( offsetParent[ 0 ], "borderLeftWidth", true );
		}

		// Subtract parent offsets and element margins
		// note: when an element has margin: auto the offsetLeft and marginLeft
		// are the same in Safari causing offset.left to incorrectly be 0
		return {
			top:  offset.top  - parentOffset.top - jQuery.css( elem, "marginTop", true ),
			left: offset.left - parentOffset.left - jQuery.css( elem, "marginLeft", true)
		};
	},

	offsetParent: function() {
		return this.map(function() {
			var offsetParent = this.offsetParent || docElem;

			while ( offsetParent && ( !jQuery.nodeName( offsetParent, "html" ) && jQuery.css( offsetParent, "position" ) === "static" ) ) {
				offsetParent = offsetParent.offsetParent;
			}
			return offsetParent || docElem;
		});
	}
});

// Create scrollLeft and scrollTop methods
jQuery.each( { scrollLeft: "pageXOffset", scrollTop: "pageYOffset" }, function( method, prop ) {
	var top = /Y/.test( prop );

	jQuery.fn[ method ] = function( val ) {
		return access( this, function( elem, method, val ) {
			var win = getWindow( elem );

			if ( val === undefined ) {
				return win ? (prop in win) ? win[ prop ] :
					win.document.documentElement[ method ] :
					elem[ method ];
			}

			if ( win ) {
				win.scrollTo(
					!top ? val : jQuery( win ).scrollLeft(),
					top ? val : jQuery( win ).scrollTop()
				);

			} else {
				elem[ method ] = val;
			}
		}, method, val, arguments.length, null );
	};
});

// Add the top/left cssHooks using jQuery.fn.position
// Webkit bug: https://bugs.webkit.org/show_bug.cgi?id=29084
// getComputedStyle returns percent when specified for top/left/bottom/right
// rather than make the css module depend on the offset module, we just check for it here
jQuery.each( [ "top", "left" ], function( i, prop ) {
	jQuery.cssHooks[ prop ] = addGetHookIf( support.pixelPosition,
		function( elem, computed ) {
			if ( computed ) {
				computed = curCSS( elem, prop );
				// if curCSS returns percentage, fallback to offset
				return rnumnonpx.test( computed ) ?
					jQuery( elem ).position()[ prop ] + "px" :
					computed;
			}
		}
	);
});


// Create innerHeight, innerWidth, height, width, outerHeight and outerWidth methods
jQuery.each( { Height: "height", Width: "width" }, function( name, type ) {
	jQuery.each( { padding: "inner" + name, content: type, "": "outer" + name }, function( defaultExtra, funcName ) {
		// margin is only for outerHeight, outerWidth
		jQuery.fn[ funcName ] = function( margin, value ) {
			var chainable = arguments.length && ( defaultExtra || typeof margin !== "boolean" ),
				extra = defaultExtra || ( margin === true || value === true ? "margin" : "border" );

			return access( this, function( elem, type, value ) {
				var doc;

				if ( jQuery.isWindow( elem ) ) {
					// As of 5/8/2012 this will yield incorrect results for Mobile Safari, but there
					// isn't a whole lot we can do. See pull request at this URL for discussion:
					// https://github.com/jquery/jquery/pull/764
					return elem.document.documentElement[ "client" + name ];
				}

				// Get document width or height
				if ( elem.nodeType === 9 ) {
					doc = elem.documentElement;

					// Either scroll[Width/Height] or offset[Width/Height] or client[Width/Height], whichever is greatest
					// unfortunately, this causes bug #3838 in IE6/8 only, but there is currently no good, small way to fix it.
					return Math.max(
						elem.body[ "scroll" + name ], doc[ "scroll" + name ],
						elem.body[ "offset" + name ], doc[ "offset" + name ],
						doc[ "client" + name ]
					);
				}

				return value === undefined ?
					// Get width or height on the element, requesting but not forcing parseFloat
					jQuery.css( elem, type, extra ) :

					// Set width or height on the element
					jQuery.style( elem, type, value, extra );
			}, type, chainable ? margin : undefined, chainable, null );
		};
	});
});


// The number of elements contained in the matched element set
jQuery.fn.size = function() {
	return this.length;
};

jQuery.fn.andSelf = jQuery.fn.addBack;




// Register as a named AMD module, since jQuery can be concatenated with other
// files that may use define, but not via a proper concatenation script that
// understands anonymous AMD modules. A named AMD is safest and most robust
// way to register. Lowercase jquery is used because AMD module names are
// derived from file names, and jQuery is normally delivered in a lowercase
// file name. Do this after creating the global so that if an AMD module wants
// to call noConflict to hide this version of jQuery, it will work.
if ( typeof define === "function" && define.amd ) {
	define( "jquery", [], function() {
		return jQuery;
	});
}




var
	// Map over jQuery in case of overwrite
	_jQuery = window.jQuery,

	// Map over the $ in case of overwrite
	_$ = window.$;

jQuery.noConflict = function( deep ) {
	if ( window.$ === jQuery ) {
		window.$ = _$;
	}

	if ( deep && window.jQuery === jQuery ) {
		window.jQuery = _jQuery;
	}

	return jQuery;
};

// Expose jQuery and $ identifiers, even in
// AMD (#7102#comment:10, https://github.com/jquery/jquery/pull/557)
// and CommonJS for browser emulators (#13566)
if ( typeof noGlobal === strundefined ) {
	window.jQuery = window.$ = jQuery;
}




return jQuery;

}));
// j2sQueryExt.js]
// BH 2022.01.12 adds pointer option
// BH 7/13/2019 removing hook for J2S.unsetMouse
// BH 7/21/2016 9:25:38 PM passing .pageX and  .pageY to jQuery event
// BH 7/24/2015 7:24:30 AM renamed from JSmoljQueryExt.js
// BH 3/11/2014 6:31:01 AM BH fix for MSIE not working locally
// BH 9/2/2013 7:43:12 AM BH Opera/Safari fix for binary file reading

;(function($) {

	var addPointerEvent = function(mode, a) {
		  a = a.split(" ");
		  for (var i = a.length; --i >= 0;)
			  $.event.special[mode+a[i]] = {bindType: "pointer" +a[i], delegateType: "pointer" + a[i]};
		}

		addPointerEvent("mouse", "up down move over out enter leave"); // BHTEST

	function createXHR(isMSIE) {
		try {
			return (isMSIE ? new window.ActiveXObject( "Microsoft.XMLHTTP" ) : new window.XMLHttpRequest());
		} catch( e ) {}
	}

 $.ajaxSettings.xhr = (window.ActiveXObject === undefined ? createXHR :  
	function() {
		return (this.url == document.location || this.url.indexOf("http") == 0 || !this.isLocal) &&  // BH MSIE fix
			/^(get|post|head|put|delete|options)$/i.test( this.type ) &&
			createXHR() || createXHR(1);
	});


// Bind script tag hack transport
		$.ajaxTransport( "+script", function(s) {

	// This transport only deals with cross domain requests
	// BH: No! This is not compatible with Chrome
	if ( true || s.crossDomain ) {

		var script,
			head = document.head || jQuery("head")[0] || document.documentElement;

		return {

			send: function( _, callback ) {
				script = document.createElement("script");
				//script.async = true;

				if ( s.scriptCharset ) {
					script.charset = s.scriptCharset;
				}

				script.src = s.url;

				// Attach handlers for all browsers
				script.onload = script.onreadystatechange = function( _, isAbort ) {

					if ( isAbort || !script.readyState || /loaded|complete/.test( script.readyState ) ) {

						// Handle memory leak in IE
						script.onload = script.onreadystatechange = null;
						// Remove the script
						if ( script.parentNode ) {
							script.parentNode.removeChild( script );
						}

						// Dereference the script
						script = null;

						// Callback if not abort
						if ( !isAbort ) {
							callback( 200, "success" );
						}
					}
				};

				// Circumvent IE6 bugs with base elements (#2709 and #4378) by prepending
				// Use native DOM manipulation to avoid our domManip AJAX trickery
				head.insertBefore( script, head.firstChild );
			},

			abort: function() {
				if ( script ) {
					script.onload( undefined, true );
				}
			}
		};
	}
});
 
	// incorporates jquery.iecors MSIE asynchronous cross-domain request for MSIE < 10

	$.extend( $.support, { iecors: !!window.XDomainRequest });

	if ($.support.iecors) {
		// source: https://github.com/dkastner/jquery.iecors
		// author: Derek Kastner dkastner@gmail.com http://dkastner.github.com    
		$.ajaxTransport(function(s) {
		
			return {
				send: function( headers, complete ) {				
					// Note that xdr is not synchronous.
					// This is only being used in JSmol for transport of java code packages.
					var xdr = new window.XDomainRequest();
					xdr.onload = function() {          
						var headers = { 'Content-Type': xdr.contentType };
						complete(200, 'OK', { text: xdr.responseText }, headers);
					};
					if ( s.xhrFields ) {
						xdr.onerror = s.xhrFields.error;
						xdr.ontimeout = s.xhrFields.timeout;
					}
					xdr.open( s.type, s.url );
					xdr.send( ( s.hasContent && s.data ) || null );
				},
				abort: function() {        
					xdr.abort();
				}
			};
		});

	} else {

	// adds support for synchronous binary file reading

		$.ajaxSetup({
			accepts: { binary: "text/plain; charset=x-user-defined" },
			responseFields: { binary: "response" }
		})


		$.ajaxTransport('binary', function(s) {
		
			var callback;
			return {
				// synchronous or asynchronous binary transfer only
				send: function( headers, complete ) {        
					var xhr = s.xhr();// BH SwingJS adds quiet option
					self.Clazz&&Clazz._quiet||console.log("xhr.open binary async=" + s.async + " url=" + s.url);
					xhr.open( s.type, s.url, s.async );					
					var isOK = false;
					try {
						if (xhr.hasOwnProperty("responseType")) {
								xhr.responseType = "arraybuffer";
								isOK = true;
						} 
					} catch(e) {
					  //
					}
					try {
						if (!isOK && xhr.overrideMimeType) {
							xhr.overrideMimeType('text/plain; charset=x-user-defined');
						}
					} catch(e) {
							//
					}
					if ( !s.crossDomain && !headers["X-Requested-With"] ) {
						headers["X-Requested-With"] = "XMLHttpRequest";
					}
					try {
						for (var i in headers )
							xhr.setRequestHeader( i, headers[ i ] );
					} catch(_) {}

					xhr.send( ( s.hasContent && s.data ) || null );

 					// Listener
					callback = function( _, isAbort ) {

					var 
						status = xhr.status,
						statusText = "",
						responseHeaders = xhr.getAllResponseHeaders(),
						responses = {},
						xml;

					try {

						// Firefox throws exceptions when accessing properties
						// of an xhr when a network error occured
						// http://helpful.knobs-dials.com/index.php/Component_returned_failure_code:_0x80040111_(NS_ERROR_NOT_AVAILABLE)
						// Was never called and is aborted or complete
						if ( callback && ( xhr.readyState === 4 ) ) {

							// Only called once
							callback = undefined;

							// When requesting binary data, IE6-9 will throw an exception
							// on any attempt to access responseText (#11426)
							try {
								responses.text = (typeof xhr.responseText === "string" ? xhr.responseText : null);
							} catch( _ ) {
							}
							try {
								responses.binary = xhr.response;
							} catch( _ ) {
							}

							// Firefox throws an exception when accessing
							// statusText for faulty cross-domain requests
							try {
								statusText = xhr.statusText;
							} catch( _ ) {
								// We normalize with Webkit giving an empty statusText
								statusText = "";
							}
							// Filter status for non standard behaviors

							// If the request is local and we have data: assume a success
							// (success with no data won't get notified, that's the best we
							// can do given current implementations)
							if ( !status && s.isLocal && !s.crossDomain ) {
								status = (responses.text ? 200 : 404);
							// IE - #1450: sometimes returns 1223 when it should be 204
							} else if ( status === 1223 ) {
								status = 204;
							}
							complete( status, statusText, responses, responseHeaders );
						}
					} catch( e ) {
						alert(e)
						complete( -1, e );
					}
					};
					
					if ( !s.async ) {
						// if we're in sync mode we fire the callback
						callback();
					} else if ( xhr.readyState === 4 ) {
						// (IE6 & IE7) if it's in cache and has been
						// retrieved directly we need to fire the callback
						setTimeout( callback );
					} else {
						// Add to the list of active xhr callbacks
						xhr.onreadystatechange = callback;
					}
					
				},
				abort: function() {}
			};
		});
	}
})( jQuery );
	 
/*
 * jQuery outside events - v1.1 - 3/16/2010
 * http://benalman.com/projects/jquery-outside-events-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 * 
 * Modified by Bob Hanson for JSmol-specific events and to add parameter reference to actual jQuery event.
 * Used for closing the pop-up menu.
 *   
 */

;(function($,doc,eventList,id){  
	// was 'click dblclick mousemove mousedown mouseup mouseover mouseout change select submit keydown keypress keyup'
	$.map(
		eventList.split(' '),
		function( event_name ) { jq_addOutsideEvent( event_name ); }
	);
	jq_addOutsideEvent( 'focusin',  'focus' + id );
	jq_addOutsideEvent( 'focusout', 'blur' + id );
	function jq_addOutsideEvent( event_name, outside_event_name ) {
		outside_event_name = outside_event_name || event_name + id;
		var elems = $(),
			event_namespaced = event_name + '.' + outside_event_name + '-special-event';
		$.event.special[ outside_event_name ] = {    
			setup: function(){
				elems = elems.add( this );
				if ( elems.length === 1 ) {
					$(doc).bind( event_namespaced, handle_event );
				}
			},
			teardown: function(){
				self.J2S && J2S.setMouseOwner(null);
				elems = elems.not( this );
				if ( elems.length === 0 ) {
					$(doc).unbind( event_namespaced );
				}
			},
			add: function( handleObj ) {
				var old_handler = handleObj.handler;
				handleObj.handler = function( event, elem, ev0) {
					event.target = elem;
          event.ev0 = ev0;
          event.pageX = ev0.pageX;
          event.pageY = ev0.pageY;
					old_handler.apply( this, arguments );
				};
			}
		};
		function handle_event( event) {
			$(elems).each(function(){
				var elem = $(this);
				if ( this !== event.target && !elem.has(event.target).length ) {
					//BH: adds event to pass that along to our handler as well.
					elem.triggerHandler( outside_event_name, [ event.target, event ] );
				}
			});
		};
	};
	// note - click is for dragging the resizer
})(jQuery,document,"click mousemove mouseup touchmove touchend", "outjsmol");
// j2sApplet.js BH = Bob Hanson hansonr@stolaf.edu

// BH 2024.11.09 makes equivalent J2S._debugCore and J2S._nozcore, as well as J2S._debugCode and J2S._nocore
// BH 2024.10.03 adds two-finger tap as "click"; reinstates touch gestures lost when we went to pointerup 2023.11.01
// BH 2023.12.14 fixes resizing into application (making it smaller)
// BH 2023.12.13 fixes RIGHT-DRAG and SHIFT-LEFT-DRAG modifier
// BH 2023.12.07 fixes mouseUp on body causing (ignorable) error
// BH 2023.11.06 adds css touch-action none
// BH 2023.11.01 adds pointerup, pointerdown, and pointermove to J2S.setMouse
// BH 2023.11.01 allows null applet in J2S.setMouse (?? should it ever be null?)
// BH 2023.02.04 adds support for file load cancel
// BH 2023.01.10 j2sargs typo
// BH 2022.08.27 fix frame resizing for browsers reporting noninteger pageX, pageY
// BH 2022.06.23 implements J2S._lastAppletID
// BH 2022.01.12 adds pointer option
// BH 2021.09.22 default file save as application/octet-stream, not text/plain
// BH 2020.12.31 full 64-bit long
// BH 2020.12.09 touch fixes for fdown and fdrag (j2sSlider)
// BH 2020.12.03 note that relay is disabled using J2S.addDirectDatabaseCall(".")
// BH 2020.04.24 Info.width includes "px" allowed and implies Info.isResizable:false; 
//               fixes early hidden 100x100 size issue due to node.offsetWidth == 0 in that case
// BH 2019.11.06 adds JFileChooser.setMultipleMode(true) and multiple-file DnD
// BH 2019.10.31 (Karsten Blankenagel) adds Info.spinnerImage: ["none"|<j2sdir/>path|/path|http[s]://path]
// BH 2019.10.20 fixes modal for popup dialog; still needs work for two applets?
// BH 2019.09.13 fixes touchend canceling click
// BH 2019.08.29 fixes mouseupoutjsmol not firing MouseEvent.MOUSE_UP
// BH 5/16/2019 fixes POST method for OuputStream
// BH 2/6/2019 adds check for non-DOM event handler in getXY
// BH 1/4/2019 moves window.thisApplet to J2S.thisApplet 

// see devnotes.txt for previous changes.

// encapsulating function
;(function(J2S, jQuery, window, document) {

// check for already loaded
	
if (J2S && J2S._version) return;


/*
 * VERSION should match what is at the end of the transpiled JavaScript .js "class" files,
 * or a message in System.err will be generated by Clazz.setTVer(ver).
 * 
 */
var VERSION = "3.3.1";


jQuery || alert("Note -- jQuery is required, but it's not defined.");

var $ = jQuery;	

// settings in user-defined J2S will be added last
	
J2S || (J2S = {
		_version: VERSION,
		_debugClip: false,
		_debugCode: false,
		_debugCore: false,
		_debugPaint: false,
		_loadcore: true,  
		_nozcore: false,
		_nooutput: false, 
		_strict: false,
		_trace: null, // =xxx to stop on message containing xxx; ="xxx" to stop on message equal to xxx
		_traceEvents: false,
		_traceMouse: false,
		_traceMouseMove: false,
		_startProfiling: false,
		_useEval: true, // false here uses new Function() in j2sClazz.js, but then that totally messes up debugging
		_verbose: false,
		_lang: null,
		_appArgs: null,
		_defaultID: 0,
   });

// for now, Clazz is a window global. Wouldn't be hard to encapsulate that, 
// but it has to be also encapsulated in Clazz. 
	
Clazz = {				   
	  _VERSION_R: J2S._version,
	  _VERSION_T: "unknown"
	};
	
var getURIField = function(name, def) {
	try {
		var ref = document.location.href.toLowerCase();
		var i = ref.indexOf(name + "=");
		if (i >= 0)
			def = decodeURI((document.location.href + "&").substring(
					i + name.length + 1).split("&")[0]);
	} catch (e) {
	} finally {
		return def;
	}
}
		
var getFlag = function(flag) {
	try{ 
		return (document.location.href.indexOf(flag) >= 0);
	}catch(e){
		return null;
	} 
};

if (getFlag("j2s")) {
	// note: these flag checks are purposely loose. "?j2smouse" will set j2smouse and j2smousemove. 
	J2S._appArgs = getURIField("j2sargs", null); // to be passed on to application
	J2S._debugClip = getFlag("j2sdebugclip");    // shows all show/restore and clip operations in JSGraphics2D
	J2S._debugPaint = getFlag("j2sdebugpaint");  // repaint manager information
	J2S._headless = getFlag("j2sheadless");      // run headlessly
	J2S._lang = getURIField("j2slang", null);    // preferred language; application should check
	 // will alert in system.out.println with a message when events occur
	J2S._nooutput = getFlag("j2snooutput");      // no System.out, only System.err message
	J2S._debugCore = J2S._nozcore = getFlag("j2sdebugcore") || getFlag("j2snozcore"); // no compressed core.z.js files
	J2S._debugCode = J2S._nocore = getFlag("j2sdebugcode") || getFlag("j2snocore"); // no core files
	J2S._loadcore = !J2S._nocore;		  
	J2S._strict = getFlag("j2sstrict");          // strict mode -- experimental
	J2S._startProfiling = getFlag("j2sprofile"); // track object creation
	J2S._traceEvents = getFlag("j2sevents");     // reports ComponentEvent instances 
	J2S._traceMouse = getFlag("j2smouse");       // mouse events, but not move
	J2S._traceMouseMove = getFlag("j2smousemove"); // mouse messages including move
	J2S._traceOutput = getURIField("j2strace");  // look only for these
	J2S._traceFilter = getURIField("j2sfilter"); // remove these
	J2S._useEval = !getFlag("j2snoeval");        // use new Function() instead of eval(); breaks debugging
	J2S._verbose = getFlag("j2sverbose");        // file loading reports

}

J2S.onClazzLoaded || (J2S.onClazzLoaded = function(i, msg) {console.log([i,msg])});

var getZOrders = function(z) {
	return {
		rear : z++,
		header : z++,
		main : z++,
		content : z++,
		front : z++,
		fileOpener : z++,
		coverImage : z++,
		dialog : z++, // could be several of these, JSV only
		menu : z + 90000, // way front
		console : z + 91000, // even more front
		consoleImage : z + 91001, // bit more front; increments
		monitorZIndex : z + 99999
	// way way front
	}
};

window.J2S = J2S = (function() {
		var z = J2S.z || 9000;
		var j = {
			Globals: {},
			setGlobal: function(a, v) { J2S.Globals[a] = v },
			getGlobal: function(a) { return J2S.Globals[a] },

			_alertNoBinary : true,
			_allowedAppletSize : [ 25, 2048, 500 ], // min, max, default
													// (pixels)
			/*
			 * By setting the J2S.allowedJmolSize[] variable in the webpage
			 * before calling SwingJS.getApplet(), limits for applet size can be
			 * overriden. 2048 standard for GeoWall
			 * (http://geowall.geo.lsa.umich.edu/home.html)
			 */
			_appletCssClass : "",
			_appletCssText : "",
			_fileCache : {}, // a simple object used only in J2S._loadFileData and J2S.loadFileAsynchronously 
			    // via J2S._checkCache and only for non-js files and only if Info.cacheFiles == true (which it is not in SwingJS)
			_javaFileCache : null, // a Hashtable, for JSUtil and /TEMP/
			_jarFile : null, // can be set in URL using _JAR=
			_j2sPath : null, // can be set in URL using _J2S=
			_use : null, // can be set in URL using _USE=
			_j2sLoadMonitorOpacity : 90, // initial opacity for j2s load
											// monitor message
			_applets : {},
			_asynchronous : true,
			_ajaxQueue : [],
			_persistentMenu : false,
			_getZOrders : getZOrders,
			_z : getZOrders(z),
			db : {
				_DirectDatabaseCalls : {
					// these sites are known to implement
					// access-control-allow-origin *
					// null here means no conversion necessary
					"INTERNET.TEST" : "https://pubchem.ncbi.nlm.nih.gov",
					"chemapps.stolaf.edu" : null,
					"cactus.nci.nih.gov" : null,
					".x3dna.org" : null,
					"rruff.geo.arizona.edu" : null,
					".rcsb.org" : null,
					"ftp.wwpdb.org" : null,
					"pdbe.org" : null,
					"materialsproject.org" : null,
					".ebi.ac.uk" : null,
					"pubchem.ncbi.nlm.nih.gov" : null,
					"www.nmrdb.org/tools/jmol/predict.php" : null,
					"jalview.org/" : null,
					"$" : "https://cactus.nci.nih.gov/chemical/structure/%FILENCI/file?format=sdf&get3d=True",
					"$$" : "https://cactus.nci.nih.gov/chemical/structure/%FILENCI/file?format=sdf",
					"=" : "https://files.rcsb.org/download/%FILE.pdb",
					"*" : "https://www.ebi.ac.uk/pdbe/entry-files/download/%FILE.cif",
					"==" : "https://files.rcsb.org/ligands/download/%FILE.cif",
					":" : "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/%FILE/SDF?record_type=3d"
				}
			},
			_debugAlert : false,
			_document : document,
			_isXHTML : false,
			_lastAppletID : null,
			_mousePageX : null,
			_mouseOwner : null,
			_serverUrl : "https://your.server.here/jsmol.php",
			_syncId : ("" + Math.random()).substring(3),
			_touching : false,
			_XhtmlElement : null,
			_XhtmlAppendChild : false
		}
		j.z = z;
		var ref = document.location.href.toLowerCase();
		j._httpProto = (ref.indexOf("https") == 0 ? "https://" : "http://");
		j._isFile = (ref.indexOf("file:") == 0);
		if (j._isFile) // ensure no attempt to read XML in local request:
			$.ajaxSetup({
				mimeType : "text/plain"
			});
		j._ajaxTestSite = j._httpProto + "google.com";
		var isLocal = (j._isFile 
				|| ref.indexOf("http://localhost") == 0 
				|| ref.indexOf("http://127.") == 0
				|| ref.indexOf("https://localhost") == 0 
				|| ref.indexOf("https://127.") == 0)
				;
		// this url is used to Google Analytics tracking of Jmol use. You may
		// remove it or modify it if you wish.
		j._tracker = (!isLocal && 'https://chemapps.stolaf.edu/jmol/JmolTracker.php?id=UA-45940799-1');

		j._isChrome = (navigator.userAgent.toLowerCase().indexOf("chrome") >= 0);
		j._isSafari = (!j._isChrome && navigator.userAgent.toLowerCase()
				.indexOf("safari") >= 0);
		j._isMsie = (window.ActiveXObject !== undefined);
		j._isEdge = (navigator.userAgent.indexOf("Edge/") >= 0);
		j._useDataURI = /*!j._isSafari && */  !j._isMsie && !j._isEdge; // safari
																	// may be OK
																	// here --
																	// untested
		j._canClickFileReader = !j._isSafari && !j._isChrome; // and others?
		
		j.htmlOverflowOriginal = null; // delayed definition

		window.requestAnimationFrame
				|| (window.requestAnimationFrame = window.setTimeout);
		for ( var i in J2S)
			j[i] = J2S[i]; // allows pre-definition
		return j;
	})();

    J2S.cantLoadLocalFiles = function() {
		alert("There was a problem loading local files. " +
				"Check to see that your browser has been set up to read local files." +
				" \n\n\n-- Developers: Override J2S.cantLoadLocalFiles to " +
				"customize this message.");
    }
    
	J2S.extend = function(map, map0, key0) {
		for (key in map) {
			var val = map[key]
			var a = (key0 ? map0[key0] : J2S);
			if (a[key] && typeof val == "object" && typeof key == "object") {
				J2S.extend(val, a, key);
				continue;
			} else {
				a = val;
			}
		}
	}

	J2S.__$ = $; // local jQuery object -- important if any other module
					// needs to access it (JSmolMenu, for example)

	// this library is organized into the following sections:

	// jQuery interface
	// protected variables
	// feature detection
	// AJAX-related core functionality
	// applet start-up functionality
	// misc core functionality
	// mouse events

	// //////////////////// jQuery interface ///////////////////////

	// hooks to jQuery -- if you have a different AJAX tool, feel free to adapt.
	// There should be no other references to jQuery in all the JSmol libraries.

	// automatically switch to returning HTML after the page is loaded
	$(document).ready(function() {
		J2S._document = null
	});

	J2S.$ = function(objectOrId, subdiv) {
		// if a subdiv, then return $("#objectOrId._id_subdiv")
		// or if no subdiv, then just $(objectOrId)
		if (objectOrId == null)
			alert(subdiv + arguments.callee.caller.toString());
		return $(subdiv ? "#" + objectOrId._id + "_" + subdiv : objectOrId);
	}

	J2S._$ = function(id) {
		// either the object or $("#" + id)
		return (typeof id == "string" ? $("#" + id) : id);
	}

	// / special functions:

	J2S.$ajax = function(info) {
		info.url = fixProtocol(info.url);
		J2S._ajaxCall = info.url;
		info.cache = (info.cache != "NO");
		return $.ajax(info);
	}

	var fixProtocol = function(url) {
		if (!J2S._isFile && url.indexOf("file://") >= 0)
			url = "http" + url.substring(4);
		// force https if page is https
		if (J2S._httpProto == "https://" && url.indexOf("http://") == 0)
			url = "https" + url.substring(4);
		return url;
	}

	J2S.$appEvent = function(app, subdiv, evt, f) {
		var o = J2S.$(app, subdiv);
		o.off(evt) && f && o.on(evt, f);
	}

	J2S.$resize = function(f) {
		return $(window).resize(f);
	}

	// // full identifier expected (could be "body", for example):

	J2S.$after = function(what, s) {
		return $(what).after(s);
	}

	J2S.$append = function(what, s) {
		return $(what).append(s);
	}

	J2S.$bind = function(what, list, f) {
		return (f ? $(what).bind(list, f) : $(what).unbind(list));
	}

	J2S.$closest = function(what, d) {
		return $(what).closest(d);
	}

	J2S.$get = function(what, i) {
		return $(what).get(i);
	}

	// element id expected

	J2S.$documentOff = function(evt, id) {
		return $(document).off(evt, "#" + id);
	}

	J2S.$documentOn = function(evt, id, f) {
		return $(document).on(evt, "#" + id, f);
	}

	J2S.$getAncestorDiv = function(id, className) {
		return $("div." + className + ":has(#" + id + ")")[0];
	}

	J2S.$supportsIECrossDomainScripting = function() {
		return $.support.iecors;
	}

	// // element id or jQuery object expected:

	J2S.$attr = function(id, a, val) {
		return J2S._$(id).attr(a, val);
	}

	J2S.$css = function(id, style) {
		return J2S._$(id).css(style);
	}

	J2S.$find = function(id, d) {
		return J2S._$(id).find(d);
	}

	J2S.$focus = function(id) {
		return J2S._$(id).focus();
	}

	J2S.$html = function(id, html) {
		return J2S._$(id).html(html);
	}

	J2S.$offset = function(id) {
		return J2S._$(id).offset();
	}

	J2S.$windowOn = function(evt, f) {
		return $(window).on(evt, f);
	}

	J2S.$prop = function(id, p, val) {
		var o = J2S._$(id);
		return (arguments.length == 3 ? o.prop(p, val) : o.prop(p));
	}

	J2S.$remove = function(id) {
		return J2S._$(id).remove();
	}

	J2S.$scrollTo = function(id, n) {
		var o = J2S._$(id);
		return o.scrollTop(n < 0 ? o[0].scrollHeight : n);
	}

	J2S.$setEnabled = function(id, b) {
		return J2S._$(id).attr("disabled", b ? null : "disabled");
	}

	J2S.$getSize = function(id) {
		var o = J2S._$(id);
		return [ o.width(), o.height() ]
	}

	J2S.$setSize = function(id, w, h) {
		return J2S._$(id).width(w).height(h);
	}

	J2S.$setVisible = function(id, b) {
		var o = J2S._$(id);
		return (b ? o.show() : o.hide());
	}

	J2S.$submit = function(id) {
		return J2S._$(id).submit();
	}

	J2S.$val = function(id, v) {
		var o = J2S._$(id);
		return (arguments.length == 1 ? o.val() : o.val(v));
	}

	// //////////// protected variables ///////////

	J2S._clearVars = function() {

		// only on page closing -- appears to improve garbage collection

		delete jQuery;
		delete $;
		delete J2S;
		delete Clazz;

		delete java;
		delete javajs;
		delete org;
		delete com;
		delete edu;

		// these are for Jmol:

		delete SwingController;
		delete J;
		delete JM;
		delete JS;
		delete JSV;
		delete JU;
		delete JV;
	}

	// //////////// feature detection ///////////////

	J2S.featureDetection = (function(document, window) {

		var features = {};
		features.ua = navigator.userAgent.toLowerCase()

		features.os = (function() {
			var osList = [ "linux", "unix", "mac", "win" ]
			var i = osList.length;

			while (i--) {
				if (features.ua.indexOf(osList[i]) != -1)
					return osList[i]
			}
			return "unknown";
		})();

		features.browser = function() {
			var ua = features.ua;
			var browserList = [ "konqueror", "webkit", "omniweb", "opera",
					"webtv", "icab", "msie", "mozilla" ];
			for (var i = 0; i < browserList.length; i++)
				if (ua.indexOf(browserList[i]) >= 0)
					return browserList[i];
			return "unknown";
		}
		features.browserName = features.browser();
		features.browserVersion = parseFloat(features.ua.substring(features.ua
				.indexOf(features.browserName)
				+ features.browserName.length + 1));
		features.supportsXhr2 = function() {
			return ($.support.cors || $.support.iecors)
		}
		features.allowDestroy = (features.browserName != "msie");
		features.allowHTML5 = (features.browserName != "msie" || navigator.appVersion
				.indexOf("MSIE 8") < 0);
		features.getDefaultLanguage = function() {
			return (J2S._lang || navigator.language || navigator.userLanguage || "en-US");
		};

		features._webGLtest = 0;

		features.supportsWebGL = function() {
			if (!J2S.featureDetection._webGLtest) {
				var canvas;
				J2S.featureDetection._webGLtest = (window.WebGLRenderingContext
						&& ((canvas = document.createElement("canvas"))
								.getContext("webgl") || canvas
								.getContext("experimental-webgl")) ? 1 : -1);
			}
			return (J2S.featureDetection._webGLtest > 0);
		};

		features.supportsLocalization = function() {
			// <meta charset="utf-8">
			var metas = document.getElementsByTagName('meta');
			for (var i = metas.length; --i >= 0;)
				if (metas[i].outerHTML.toLowerCase().indexOf("utf-8") >= 0)
					return true;
			return false;
		};

		features.supportsJava = function() {
			if (!J2S.featureDetection._javaEnabled) {
				if (J2S._isMsie) {
					if (!navigator.javaEnabled()) {
						J2S.featureDetection._javaEnabled = -1;
					} else {
						// more likely -- would take huge testing
						J2S.featureDetection._javaEnabled = 1;
					}
				} else {
					J2S.featureDetection._javaEnabled = (navigator
							.javaEnabled()
							&& (!navigator.mimeTypes || navigator.mimeTypes["application/x-java-applet"]) ? 1
							: -1);
				}
			}
			return (J2S.featureDetection._javaEnabled > 0);
		};

		features.compliantBrowser = function() {
			var a = !!document.getElementById;
			var os = features.os;
			// known exceptions (old browsers):
			if (features.browserName == "opera"
					&& features.browserVersion <= 7.54 && os == "mac"
					|| features.browserName == "webkit"
					&& features.browserVersion < 125.12
					|| features.browserName == "msie" && os == "mac"
					|| features.browserName == "konqueror"
					&& features.browserVersion <= 3.3)
				a = false;
			return a;
		}

		features.isFullyCompliant = function() {
			return features.compliantBrowser() && features.supportsJava();
		}

		features.useIEObject = (features.os == "win"
				&& features.browserName == "msie" && features.browserVersion >= 5.5);
		features.useHtml4Object = (features.browserName == "mozilla" && features.browserVersion >= 5)
				|| (features.browserName == "opera" && features.browserVersion >= 8)
				|| (features.browserName == "webkit"/*
													 * &&
													 * features.browserVersion >=
													 * 412.2 &&
													 * features.browserVersion <
													 * 500
													 */); // 500
																																// is a
																																// guess;
																																// required
																																// for
																																// 536.3

		features.hasFileReader = (window.File && window.FileReader);

		return features;

	})(document, window);

	J2S.getDefaultLanguage = function(isAll) {
		return (isAll ? J2S.featureDetection.getDefaultLanguage() : J2S._lang)
	};

	// //////////// AJAX-related core functionality //////////////

	J2S._ajax = function(info) {
		if (!info.async) {
			info.xhr = J2S.$ajax(info);
			return info.xhr.responseText;
		}
		J2S._ajaxQueue.push(info)
		if (J2S._ajaxQueue.length == 1)
			J2S._ajaxDone()
	}
	J2S._ajaxDone = function() {
		var info = J2S._ajaxQueue.shift();
		info && (info.xhr = J2S.$ajax(info));
	}

	J2S._loadSuccess = function(a, fSuccess) {
		if (!fSuccess)
			return;
		J2S._ajaxDone();
		fSuccess(a);
	}

	J2S._loadError = function(fError) {
		J2S._ajaxDone();
		J2S.say("Error connecting to server: " + J2S._ajaxCall);
		null != fError && fError()
	}

	J2S._isDatabaseCall = function(query) {
		return (J2S.db._databasePrefixes.indexOf(query.substring(0, 1)) >= 0);
	}

	J2S.addDirectDatabaseCall = function(domain) {
		J2S.db._DirectDatabaseCalls[domain] = null;
	}

	J2S._getDirectDatabaseCall = function(query, checkXhr2) {
		if (checkXhr2 && !J2S.featureDetection.supportsXhr2())
			return query;
		var pt = 2;
		var db;
		var call = J2S.db._DirectDatabaseCalls[query.substring(0, pt)]
				|| J2S.db._DirectDatabaseCalls[db = query.substring(0, --pt)];
		if (call) {
			if (db == ":") {
				var ql = query.toLowerCase();
				if (!isNaN(parseInt(query.substring(1)))) {
					query = "cid/" + query.substring(1);
				} else if (ql.indexOf(":smiles:") == 0) {
					call += "?POST?smiles=" + query.substring(8);
					query = "smiles";
				} else if (ql.indexOf(":cid:") == 0) {
					query = "cid/" + query.substring(5);
				} else {
					if (ql.indexOf(":name:") == 0)
						query = query.substring(5);
					else if (ql.indexOf(":cas:") == 0)
						query = query.substring(4);
					query = "name/" + encodeURIComponent(query.substring(pt));
				}
			} else {
				query = encodeURIComponent(query.substring(pt));
			}
			if (call.indexOf("FILENCI") >= 0) {
				query = query.replace(/\%2F/g, "/");
				query = call.replace(/\%FILENCI/, query);
			} else {
				query = call.replace(/\%FILE/, query);
			}
		}
		return query;
	}

	J2S.fixDim = function(x, units) {
		var sx = "" + x;
		return (sx.length == 0 ? (units ? "" : J2S._allowedAppletSize[2])
				: sx.indexOf("%") == sx.length - 1 ? sx
						: (x = parseFloat(x)) <= 1 && x > 0 ? x * 100 + "%"
								: (isNaN(x = Math.floor(x)) ? J2S._allowedAppletSize[2]
										: x < J2S._allowedAppletSize[0] ? J2S._allowedAppletSize[0]
												: x > J2S._allowedAppletSize[1] ? J2S._allowedAppletSize[1]
														: x)
										+ (units ? units : ""));
	}

	J2S._getRawDataFromServer = function(database, query, fSuccess, fError,
			asBase64, noScript, infoRet) {
		// note that this method is now only enabled for "_"
		// server-side processing of database queries was too slow and only
		// useful for
		// the IMAGE option, which has been abandoned.

console.log("J2S._getRawDataFromServer " + J2S._serverUrl + " for " + query);
if (database == "_" && J2S._serverUrl.indexOf("//your.server.here/") >= 0) {
	  J2S.say("Info.serverURL has not been set. The url " + query + " is in a domain that requires Cross Origin Resource Sharing (CORS) access from this page, " +
	  		"and that domain is not recognized by SwingJS as allowing CORS access. If the server does allow CORS, " +
	  		"then the developer of this page must make sure that " +
	  		"J2S.addDirectDatabaseCall(path)" +
	  		" is called for that domain -- for example, J2S.addDirectDatabaseCall('stolaf.edu')." +
	  		" And if that domain does not allow CORS, then the developer should set Info.serverURL appropriately or contact the administrator of that domain to see if CORS can be allowed.");
	  return "";
	}

	var s = "?call=getRawDataFromDatabase&database="
				+ database
				+ (query.indexOf("?POST?") >= 0 ? "?POST?" : "")
				+ "&query="
				+ encodeURIComponent(query.replace(/ /g,"%20"))
				+ (asBase64 ? "&encoding=base64" : "")
				+ (noScript ? "" : "&script="
						+ encodeURIComponent(J2S._getScriptForDatabase(database)));
		return J2S._contactServer(s, fSuccess, fError, infoRet);
	}

	J2S._checkFileName = function(applet, fileName, isRawRet) {
		if (J2S._isDatabaseCall(fileName)) {
			if (isRawRet && J2S._setQueryTerm)
				J2S._setQueryTerm(applet, fileName);
			fileName = J2S._getDirectDatabaseCall(fileName, true);
			if (J2S._isDatabaseCall(fileName)) {
				// xhr2 not supported (MSIE)
				fileName = J2S._getDirectDatabaseCall(fileName, false);
				isRawRet && (isRawRet[0] = true);
			}
		}
		return fileName;
	}
	
	J2S.fixCachePath = function(uri) {
		if (uri.startsWith("./"))
			uri = "/" + uri;
		var n = (uri.startsWith("https:/") || uri.startsWith("file://") ? 7 
				: uri.startsWith("http:/") || uri.startsWith("file:/") ? 6
						: 0);
		if (n > 0)
			uri = uri.substring(n);
		uri = uri.replace("//", "/");
		var pt;
		while ((pt = uri.indexOf("/././")) >= 0) {
			// https://././xxx --> /./xxx
			uri = uri.substring(0, pt) + uri.substring(pt + 2);
		}
		if (uri.startsWith("/"))
			uri = uri.substring(1);
		if (uri.startsWith("./"))
			uri = uri.substring(2);
		return uri;
	}

	J2S._checkCache = function(applet, fileName, fSuccess) {
		if (applet._cacheFiles && !fileName.endsWith(".js")) {
			var data = J2S._fileCache[fileName];
			if (data) {
				System.out.println("j2sApplet using " + (data.length)
						+ " bytes of cached data for " + fileName);
				fSuccess(data);
				return null;
			} else {
				fSuccess = function(fileName, data) {
					fSuccess(J2S._fileCache[fileName] = data)
				};
			}
		}
		return fSuccess;
	}

	J2S.getSetJavaFileCache = function(map) {
		// called by swingjs.JSUtil
		if (map == null && !J2S._javaFileCache)
			J2S._javaFileCache = Clazz.new_("java.util.Hashtable");
		return (map == null ? J2S._javaFileCache : (J2S._javaFileCache = map));
	}

	J2S.getCachedJavaFile = function(key) {
		// called by Jmol FileManager
		if (!J2S._javaFileCache) return null;
		var data = J2S._javaFileCache.get$O(key);
		if (data == null && key.indexOf("file:/") == 0)
			data = J2S._javaFileCache.get$O(key.substring(6));
		return data;
	}

	J2S._checkAjaxPost = function(info) {
		var pt = info.url.indexOf("?POST?");
		if (pt > 0) {
			info.data = info.url.substring(pt + 6);
			info.url = info.url.substring(0, pt);
			info.type = "POST";
			info.contentType = "application/x-www-form-urlencoded";
		}
	}
	J2S._contactServer = function(data, fSuccess, fError, info) {
		info || (info = {});
		info.dataType = "text";
		info.type = "GET";
		info.url = J2S._serverUrl + data;
		info.success = function(a) { J2S._loadSuccess(a, fSuccess) };
		info.error = function() { J2S._loadError(fError) };
		info.async = (fSuccess ? J2S._asynchronous : false);
		J2S._checkAjaxPost(info);
		return J2S._ajax(info);
	}

	J2S._syncBinaryOK = "?";

	J2S._canSyncBinary = function(isSilent) {
		if (J2S._isAsync)
			return true;
		if (window.VBArray) // VisualBasic array MSIE 6-10
			return (J2S._syncBinaryOK = false);
		if (J2S._syncBinaryOK != "?")
			return J2S._syncBinaryOK;
		J2S._syncBinaryOK = true;
		try {
			var xhr = new window.XMLHttpRequest();
			xhr.open("text", J2S._ajaxTestSite, false);
			if (xhr.hasOwnProperty("responseType")) {
				xhr.responseType = "arraybuffer";
			} else if (xhr.overrideMimeType) {
				xhr.overrideMimeType('text/plain; charset=x-user-defined');
			}
		} catch (e) {
			var s = "JSmolCore.js: synchronous binary file transfer is requested but not available";
			System.out.println(s);
			if (J2S._alertNoBinary && !isSilent)
				alert(s)
			return J2S._syncBinaryOK = false;
		}
		return true;
	}

	J2S._binaryTypes = [ ".uk/pdbe/densities/", ".bcif?", ".au?", ".mmtf?",
			".gz?", ".jpg?", ".jpeg?", ".gif?", ".png?", ".zip?", ".jmol?",
			".bin?", ".smol?", ".spartan?", ".mrc?", ".pse?", ".map?",
			".omap?", ".dcd?", ".mp3?", ".ogg?", ".wav?", ".au?" ];

	J2S.addBinaryFileType = function(ext) {
		if (!ext.indexOf(".") == 0)
			ext = "." + ext;
		if (!ext.indexOf("?") == ext.length - 1)
			ext += "?";
		for (var i = J2S._binaryTypes.length; --i >= 0;)
			if (J2S._binaryTypes[i] == ext)
				return;
		J2S._binaryTypes.push(ext);
	}
	
	J2S.isBinaryUrl = function(url) {
		url = url.toLowerCase() + "?";
		for (var i = J2S._binaryTypes.length; --i >= 0;)
			if (url.indexOf(J2S._binaryTypes[i]) >= 0)
				return true;
		return false;
	}

	var knownDomains = {};

	// old? for Jmol? Not used in SwingJS 
	J2S._loadFileData = function(applet, fileName, fSuccess, fError, info) {
		info || (info = {});
		var isRaw = [];
		fileName = J2S._checkFileName(applet, fileName, isRaw);
		fSuccess = J2S._checkCache(applet, fileName, fSuccess);
		if (isRaw[0]) {
			J2S._getRawDataFromServer("_", fileName, fSuccess, fError, info);
			return;
		}
		info.type = "GET";
		info.dataType = "text";
		info.url = fileName;
		info.async = J2S._asynchronous;
		info.success = function(a) { J2S._loadSuccess(a, fSuccess) };
		info.error = function() { J2S._loadError(fError) };
		J2S._checkAjaxPost(info);
		J2S._ajax(info);
	}

	J2S.doAjax = function(url, postOut, dataOut, info) {
		//from AjaxURLConnection
		if (info === true)
			info = {isBinary: true};
		info || (info = {});
		// called by org.J2S.awtjs2d.JmolURLConnection.doAjax()
		url = url.toString();
		if (dataOut) {
			if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0)
				return J2S.saveFile(url, dataOut);
			info.async = false;
			info.url = url;
			info.type = "POST";
			info.processData = false;
			info.data = dataOut;//(typeof data == "string" ? dataOut : ";base64," + Clazz.load("javajs.util.Base64").getBase64$BA(dataOut).toString());
			info.xhr = J2S.$ajax(info);
			return info.xhr.responseText;
		}
		if (postOut)
			url += "?POST?" + postOut;
		return J2S.getFileData(url, info.fWhenDone, true, info);
	}

	J2S.getFileData = function(fileName, fWhenDone, doProcess, info) {
		if (info === true)
			info = {isBinary: true};
		info || (info = {});
		var noProxy = !!info.j2sNoProxy;
		if (noProxy)
			delete info.j2sNoProxy;
		var isTyped = !!info.dataType;
		var isBinary = info.isBinary;
		// swingjs.api.J2SInterface
		// use host-server PHP relay if not from this host

		if (fileName.indexOf("/") == 0)
			fileName = "." + fileName;
		else if (fileName.indexOf("https://./") == 0)
			fileName = fileName.substring(10);
		else if (fileName.indexOf("http://./") == 0)
			fileName = fileName.substring(9);
		else if (fileName.indexOf("file:/") >= 0 
				&& Clazz.loadClass("swingjs.JSUtil") != null
				&& fileName.indexOf(swingjs.JSUtil.getAppletDocumentPath$()) != 0
				&& fileName.indexOf(swingjs.JSUtil.getAppletCodePath$()) != 0)
			fileName = "./" + fileName.substring(5);
		isBinary = (isBinary || J2S.isBinaryUrl(fileName));
		var isPDB = !noProxy && (fileName.indexOf("pdb.gz") >= 0 && fileName
				.indexOf("//www.rcsb.org/pdb/files/") >= 0);
		var asBase64 = !noProxy && (isBinary && !J2S._canSyncBinary(isPDB));
		if (asBase64 && isPDB) {
			// avoid unnecessary binary transfer
			fileName = fileName.replace(/pdb\.gz/, "pdb");
			asBase64 = isBinary = false;
		}
		var isPost = (fileName.indexOf("?POST?") >= 0);
		if (fileName.indexOf("file:/") == 0
				&& fileName.indexOf("file:///") != 0)
			fileName = "file://" + fileName.substring(5); // / fixes IE
															// problem
		var isFile = (fileName.indexOf("file://") == 0);
		var isMyHost = (fileName.indexOf("://") < 0 || fileName
				.indexOf(document.location.protocol) == 0
				&& fileName.indexOf(document.location.host) >= 0);
		var isHttps2Http = (J2S._httpProto == "https://" && fileName.indexOf("http://") == 0);
		var cantDoSynchronousLoad = (!isMyHost && J2S.$supportsIECrossDomainScripting());
		var mustCallHome = !noProxy && !isFile && (isHttps2Http || asBase64 || !fWhenDone && cantDoSynchronousLoad);
		var url;
		var isNotDirectCall = !noProxy && !mustCallHome && !isFile && !isMyHost && !(url = J2S._isDirectCall(fileName));
		fileName = url || fileName;
		var data = null;
		var error = null;
		var success = null;
		if (fWhenDone) {
			success = function(data) { fWhenDone(isTyped ? data : J2S._strToBytes(data)) };
			error = function() { fWhenDone(null) };
		}
		if (mustCallHome || isNotDirectCall) {
			data = J2S._getRawDataFromServer("_", fileName, success, error,
					asBase64, true, info);
		} else {
			fileName = fileName.replace(/file:\/\/\/\//, "file://"); // opera
			info.async = !!fWhenDone;
			if (!isTyped)info.dataType = (isBinary ? "binary" : "text");
			if (noProxy) {
				info.url = fileName;
			} else if (isPost) {
				info.type = "POST";
				info.url = fileName.split("?POST?")[0]
				info.data = fileName.split("?POST?")[1]
			} else {
				!info.type && (info.type = "GET");
				info.url = fileName;
			}
			if (fWhenDone) {
				if (isBinary) {
					info.success = success;
					info.error = error;
				} else {
					// BH don't know why this is so complicated
					info.success = function(data) { fWhenDone(J2S._xhrReturn(info.xhr)) };
					info.error = function() { fWhenDone(info.xhr.statusText) };
				}
			}
			info.xhr = J2S.$ajax(info);
			if (!fWhenDone) {
				data = J2S._xhrReturn(info.xhr);
				if (data == null)
					doProcess = null; 
			}
		}
		if (!doProcess)
			return data;
		if (data == null) {
			data = "";
			isBinary = false;
		}
		isBinary && (isBinary = J2S._canSyncBinary(true));
		return (isTyped ? data : isBinary ? J2S._strToBytes(data) : (self.JU || javajs && javajs.util).SB.newS$S(data));
	}

	J2S._xhrReturn = function(xhr) {
		if (xhr.state() == "rejected" && !xhr.responseText)
			return null;
		if (!xhr.responseText && !xhr.responseJSON 
				|| Clazz.instanceOf(xhr.response, self.ArrayBuffer)) {
			// Safari or error
			return xhr.response || xhr.statusText;
		}
	    if (xhr.responesJSON)
	    	xhr.responseText = null;
		return xhr.responseJSON || xhr.responseText;
	}

	J2S._isDirectCall = function(url) {
		if (url.indexOf("://localhost") >= 0)
			return true;
		for ( var key in J2S.db._DirectDatabaseCalls) {
			if (key.indexOf(".") >= 0 && url.indexOf(key) >= 0) {
				// hack because ebi is not returning ajax calls
				return J2S.db._DirectDatabaseCalls[key] || url;//url.indexOf(".ebi.ac.") < 0 || url.indexOf("dbfetch/dbfetch") < 0;
								
			}
		}
		return false;
	}

	J2S._cleanFileData = function(data) {
		if (data.indexOf("\r") >= 0 && data.indexOf("\n") >= 0) {
			return data.replace(/\r\n/g, "\n");
		}
		if (data.indexOf("\r") >= 0) {
			return data.replace(/\r/g, "\n");
		}
		return data;
	};

	J2S._getFileType = function(name) {
		var database = name.substring(0, 1);
		if (database == "$" || database == ":")
			return "MOL";
		if (database == "=")
			return (name.substring(1, 2) == "=" ? "LCIF" : "PDB");
		// just the extension, which must be PDB, XYZ..., CIF, or MOL
		name = name.split('.').pop().toUpperCase();
		return name.substring(0, Math.min(name.length, 3));
	};

	J2S.getZ = function(applet, what) {
		return applet && applet._z && applet._z[what] || J2S._z[what];
	}

	J2S._incrZ = function(applet, what) {
		return applet && applet._z && ++applet._z[what] || ++J2S._z[what];
	}

	J2S.loadFileAsynchronously = function(fileLoadThread, applet, fileName,
			appData) {
		if (fileName.indexOf("?") != 0) {
			// LOAD ASYNC command
			var fileName0 = fileName;
			fileName = J2S._checkFileName(applet, fileName);
			var fSuccess = function(data) {
				J2S._setData(fileLoadThread, fileName, fileName0, data,	appData)
			};
			fSuccess = J2S._checkCache(applet, fileName, fSuccess);
			if (fileName.indexOf("|") >= 0)
				fileName = fileName.split("|")[0];
			return (fSuccess == null ? null : J2S.getFileData(fileName,
					fSuccess));
		}
		// we actually cannot suggest a fileName, I believe.
		if (!J2S.featureDetection.hasFileReader)
			return fileLoadThread.setData$S$S$O$O(
					"Local file reading is not enabled in your browser", null,
					null, appData);
		if (!applet._localReader) {
			var div = '<div id="ID" style="z-index:'
					+ (applet._isApp ? "100000" : J2S.getZ(applet, "fileOpener"))
					+ ';position:absolute;background:#E0E0E0;left:10px;top:10px"><div style="margin:5px 5px 5px 5px;"><input type="file" id="ID_files" /><button id="ID_loadfile">load</button><button id="ID_cancel">cancel</button></div><div>'
			J2S.$after(
					//"#" + applet._id + "_appletdiv", 
					"body", div.replace(/ID/g,
					applet._id + "_localReader"));
			applet._localReader = J2S.$(applet, "localReader");
		}
		J2S.$appEvent(applet, "localReader_loadfile", "click");
		J2S.$appEvent(applet, "localReader_loadfile", "click", function(evt) {
			var file = J2S.$(applet, "localReader_files")[0].files[0];
			var reader = new FileReader();
			reader.onloadend = function(evt) {
				if (evt.target.readyState == FileReader.DONE) { // DONE == 2
					J2S.$css(J2S.$(applet, "localReader"), {
						display : "none"
					});
					J2S._setData(fileLoadThread, file.name, file.name,
							evt.target.result, appData);
				}
			};
			reader.readAsArrayBuffer(file);
		});
		J2S.$appEvent(applet, "localReader_cancel", "click");
		J2S.$appEvent(applet, "localReader_cancel", "click", function(evt) {
			J2S.$css(J2S.$(applet, "localReader"), {
				display : "none"
			});
			fileLoadThread.setData$S$S$O$O("#CANCELED#", null, null, appData);
		});
		J2S.$css(J2S.$(applet, "localReader"), {
			display : "block"
		});
	}

	J2S._setData = function(fileLoadThread, filename, filename0, data, appData) {
		data = J2S._strToBytes(data);
		if (filename.indexOf(".jdx") >= 0)
			J2S.Cache.put("cache://" + filename, data);
		fileLoadThread.setData$S$S$O$O(filename, filename0, data, appData);
	}

	J2S._toBytes = function(data) {
		if (typeof data == "string")
			return data.getBytes$();
		// ArrayBuffer assumed here
		data = new Uint8Array(data);
		var b = Clazz.array(Byte.TYPE, data.length);
		for (var i = data.length; --i >= 0;)
			b[i] = data[i];
		return b;
	}

	/**
	 * fDone: callback function, in the form of fDone(data, fileName). Note that
	 * this can be a Java Runnable.run(), as a j2sNative call can still read the
	 * arguments.
	 * 
	 * format: "ArrayBuffer" for the raw array, "string" for a string,
	 * "java.util.Map" meaning something with a get$O(key) method that is
	 * looking for fileName:string and bytes:byte[], or anything else for byte[]
	 * directly.
	 * 
	 * parentDiv: div id in which to insert this div, or null to use body
	 */
	J2S.getFileFromDialog = function(fDone, format, parentDiv) {
		// NOTE: JavaScript will not return any notification of CANCEL
		// streamlined file dialog using <input type="file">.click()
		format || (format = "string");
		var id = "filereader" + ("" + Math.random()).split(".")[1]
		var nfiles = 1;
		var map = (format == "java.util.Map" ? Clazz.new_("java.util.Hashtable") : null);
		var arr = (format == "java.util.Array" ? Clazz.array(Clazz.new_("java.io.File"), [0]) : null);
		var isMultiple = !!(map || arr);
		var readFiles = function(files) {
				nfiles = files.length;
				for (var i = 0; i < nfiles; i++) {
					readFile(files[i]);
				}
			};
		var readFile = function(file) {
			Clazz.loadClass("swingjs.JSUtil");
			var reader = new FileReader();
			reader.onloadend = function(evt) {
				var data = null;
				if (evt.target.readyState == FileReader.DONE) {
					var data = evt.target.result;
					System.out.println("j2sApplet J2S.getFileFromDialog format=" + format 
								+ " file name=" + file.name  + " size=" + (data.length || data.byteLength));
					switch (format) {
					case "java.util.Map":
						map.put$O$O(file.name, J2S._toBytes(data));
						data = map;
						break;
					case "java.util.Array":
						var e = Clazz.new_(Clazz.load("java.io.File").c$$S,
								[ file.name ]);
						swingjs.JSUtil.setFileBytesStatic$O$O(e, J2S._toBytes(data))
						arr.push(e);
						data = arr;
						break;
					case "java.io.File":
						var f = Clazz.new_(Clazz.load("java.io.File").c$$S,
								[ J2S.getGlobal("j2s.tmpdir") + "OPEN/" + file.name ]);
						swingjs.JSUtil.setFileBytesStatic$O$O(f, J2S._toBytes(data));
						data = f;
						break;
					case "ArrayBuffer":
						break;
					case "string":
						data = String.instantialize(data);
						break;
					default:
						data = J2S._toBytes(data);
						break;
					}
				}
				if (--nfiles == 0) {
					J2S.$remove(id);
					J2S.$remove("_filereader_modalscreen");
					reader = null;
					fDone(data, file.name);
				}
			};
			reader.readAsArrayBuffer(file);
		};

		// x.click() in any manifestation will not work from Chrome or Safari.
		// These browers require that the user see and click the link.
		if (J2S._canClickFileReader) {
			var x = document.createElement("input");
			x.value = x.text = "xxxx";
			x.type = "file";
			if (isMultiple)
				x.setAttribute("multiple", "true");
			x.addEventListener("change", function(ev) {
				J2S._fileReaderCancelListener = null;
				window.removeEventListener("focus", J2S._fileReaderCancelListener);
				(isMultiple ? readFiles(this.files) : readFile(this.files[0]));
			}, false);			
			x.click();
			window.addEventListener("focus", J2S._fileReaderCancelListener = function(a){
				setTimeout(function() {
					window.removeEventListener("focus", J2S._fileReaderCancelListener);
					if (J2S._fileReaderCancelListener != null) {
						J2S._fileReaderCancelListener = null;
						fDone(null, null);
					}
				},500)
			});
		} else {
			var px = screen.width / 2 - 180; 
			var py = screen.height / 2 - 40; 
			var div = ('<div id="ID" style="z-index:1000000;position:fixed;background:#E0E0E0;left:' + px + 'px;top:' + py + 'px">'
					+ '<div style="margin:5px 5px 5px 5px;">'
					+ '<input type="file" id="ID_files" ' + (isMultiple ? ' multiple="multiple"' :'')+')/>'
					+ '<button id="ID_loadfile">load</button>'
					+ '<button id="ID_cancel">cancel</button>' + '</div>' + '<div>')
					.replace(/ID/g, id);
			var parent = "body";//(!parentDiv || parentDiv == "body" ? "body"
					//: typeof parentDiv == "string" ? "#" + parentDiv
						//	: parentDiv);
			if (parent == "body") {
				J2S.$after(document.body, div);
				J2S.$after(document.body, '<div id="_filereader_modalscreen" style="z-index:999999;background:rgba(100,100,100,0.4);position:fixed;left:0;top:0;width:100%;height:100%;"></div>')
			} else {
				J2S.$append(parent, div);
			}
			J2S.$appEvent("#" + id + "_loadfile", null, "click");
			J2S.$appEvent("#" + id + "_loadfile", null, "click", function(evt) {
				readFiles(J2S.$("#" + id + "_files")[0].files);
			});
			J2S.$appEvent("#" + id + "_cancel", null, "click");
			J2S.$appEvent("#" + id + "_cancel", null, "click", function(evt) {
				J2S.$remove(id);
				J2S.$remove("_filereader_modalscreen");
				fDone(null, null);
			});
			J2S.$css(J2S.$("#" + id), {
				display : "block"
			});
		}
	}

	// J2S._localFileSaveFunction -- // do something local here; Maybe try the
	// FileSave interface? return true if successful

	J2S.saveFile = J2S._saveFile = function(filename, data, mimetype, encoding) {
		var isString = (typeof data == "string");
		if (filename.indexOf(J2S.getGlobal("j2s.tmpdir")) == 0) {
			return J2S.getSetJavaFileCache().put$O$O(J2S.fixCachePath(filename), (isString ? data.getBytes$S("UTF-8") : data));
		}
		if (J2S._localFileSaveFunction
				&& J2S._localFileSaveFunction(filename, data))
			return "OK";
		var filename = filename.substring(filename.lastIndexOf("/") + 1);
		mimetype
				|| (mimetype = (filename.indexOf(".pdf") >= 0 ? "application/pdf"
						: filename.indexOf(".zip") >= 0 ? "application/zip"
								: filename.indexOf(".png") >= 0 ? "image/png"
										: filename.indexOf(".gif") >= 0 ? "image/gif"
												: filename.indexOf(".jpg") >= 0
														| filename
																.indexOf(".jpeg") >= 0 ? "image/jpg"
														: ""));
		data = Clazz.loadClass("javajs.util.Base64").getBase64$BA(
				isString ? data.getBytes$S("UTF-8") : data).toString();
		encoding || (encoding = "base64");
		var url = J2S._serverUrl;
		url && url.indexOf("your.server") >= 0 && (url = "");
		if (J2S._useDataURI || !url) {
			// Asynchronous output generated using an anchor tag
			var a = document.createElement("a");
			a.href = "data:" + mimetype + ";base64," + data;
			a.type = mimetype || (mimetype = "application/octet-stream");//was "text/plain;charset=utf-8");
			a.download = filename;
			a.target = "_blank";
			$("body").append(a);
			a.click();
			a.remove();
		} else {
			// Asynchronous output to be reflected as a download
			if (!J2S._formdiv) {
				var sform = '<div id="__jsmolformdiv__" style="display:none">\
						<form id="__jsmolform__" method="post" target="_blank" action="">\
						<input name="call" value="saveFile"/>\
						<input id="__jsmolmimetype__" name="mimetype" value=""/>\
						<input id="__jsmolencoding__" name="encoding" value=""/>\
						<input id="__jsmolfilename__" name="filename" value=""/>\
						<textarea id="__jsmoldata__" name="data"></textarea>\
						</form>\
						</div>'
				J2S.$after("body", sform);
				J2S._formdiv = "__jsmolform__";
			}
			J2S.$attr(J2S._formdiv, "action", url + "?"
					+ (new Date()).getMilliseconds());
			J2S.$val("__jsmoldata__", data);
			J2S.$val("__jsmolfilename__", filename);
			J2S.$val("__jsmolmimetype__", mimetype);
			J2S.$val("__jsmolencoding__", encoding);
			J2S.$submit("__jsmolform__");
			J2S.$val("__jsmoldata__", "");
			J2S.$val("__jsmolfilename__", "");
		}
		return "OK";
	}

	J2S._strToBytes = function(s) {
		if (Clazz.instanceOf(s, self.ArrayBuffer))
			return J2S._toBytes(s);
		if (s.indexOf(";base64,") == 0) {
			return Clazz.loadClass("javajs.util.Base64").decodeBase64$S(
					s.substring(8));
		}
		// 		return Clazz.array(new Int8Array(new TextEncoder().encode(s).buffer),Clazz.array(Byte.TYPE));
		// not UTF-8 - this is a Chrome encoding of the bytes as "string"
		var b = Clazz.array(Byte.TYPE, s.length);
		for (var i = s.length; --i >= 0;)
			b[i] = s.charCodeAt(i) & 0xFF;
		return b;
	}

	// //////////// applet start-up functionality //////////////

	J2S.findApplet = function(name) {
		// swingjs.api.J2SInterface
		return J2S._applets[name.split("_object")[0]];
	}

	J2S.getJavaVersion = function() {
		// swingjs.api.J2SInterface
		return J2S._version;
	}

	J2S._setAppletThread = function(appletName, myThread) {
		// swingjs.api.J2SInterface
		J2S._applets[appletName + "_thread"] = myThread;
	}

	J2S._setConsoleDiv = function(d) {
		Clazz.setConsoleDiv && Clazz.setConsoleDiv(d);
	}

	J2S.setWindowVar = function(id, applet) {
		// could be modified for use in fully encapsulated version
		return window[id] = applet;
	}
	
	J2S._registerApplet = function(id, applet) {
		// note - I am leaving thisApplet in for now, but it is to be deprecated 1/4/2019
		return J2S.setWindowVar(id, thisApplet = J2S.thisApplet = J2S._applets[id] = J2S._applets[id
				+ "__" + J2S._syncId + "__"] = J2S._applets["master"] = applet);
	}

	J2S.readyCallback = function(appId, fullId, isReady, javaApplet,
			javaAppletPanel) {
		// swingjs.api.J2SInterface
		appId = appId.split("_object")[0];
		var applet = J2S._applets[appId];
		isReady = (isReady.booleanValue ? isReady.booleanValue() : isReady);
		// necessary for MSIE in strict mode -- apparently, we can't call
		// J2S.readyCallback, but we can call J2S.readyCallback. Go figure...
		if (isReady) {
			// applet._appletPanel is set in SwingJSApplet upon creation
			applet._appletPanel || (applet._appletPanel = (javaAppletPanel || javaApplet));
			// when leaving page, Java applet may be dead
			applet._applet = javaApplet;
			!applet.getApp && (applet.getApp = function(){ applet._setThread();return javaApplet });
			J2S.$css(J2S.$(applet, 'appletdiv'), { 'background-image': '' });
		} else {
			applet.getApp = null;
			applet._applet = null;
			applet._appletPanel = null;
		}
		J2S._track(applet.readyCallback(appId, fullId, isReady));
	}

	J2S._getWrapper = function(applet, isHeader) {

		// id_appletinfotablediv
		// id_appletdiv
		// id_coverdiv
		// id_infotablediv
		// id_infoheaderdiv
		// id_infoheaderspan
		// id_infocheckboxspan
		// id_infodiv

		// for whatever reason, without DOCTYPE, with MSIE, "height:auto" does
		// not work,
		// and the text scrolls off the page.
		// So I'm using height:95% here.
		// The table was a fix for MSIE with no DOCTYPE tag to fix the
		// miscalculation
		// in height of the div when using 95% for height.
		// But it turns out the table has problems with DOCTYPE tags, so that's
		// out.
		// The 95% is a compromise that we need until the no-DOCTYPE MSIE
		// solution is found.
		// (100% does not work with the JME linked applet)
		var s;
		// ... here are just for clarification in this code; they are removed
		// immediately
		if (isHeader) {
			var img = "";
			if (applet._coverImage) {
				var more = " onclick=\"J2S.coverApplet(ID, false)\" title=\""
						+ (applet._coverTitle) + "\"";
				var play = "<image id=\"ID_coverclickgo\" src=\""
						+ applet._j2sPath
						+ "/img/play_make_live.jpg\" style=\"width:25px;height:25px;position:absolute;bottom:10px;left:10px;"
						+ "z-index:" + J2S.getZ(applet, "coverImage")
						+ ";opacity:0.5;\"" + more + " />"
				img = "<div id=\"ID_coverdiv\" style=\"background-color:red;z-index:"
						+ J2S.getZ(applet, "coverImage")
						+ ";width:100%;height:100%;display:inline;position:absolute;top:0px;left:0px\"><image id=\"ID_coverimage\" src=\""
						+ applet._coverImage
						+ "\" style=\"width:100%;height:100%\""
						+ more
						+ "/>"
						+ play + "</div>";
			}
			var css = J2S._appletCssText.replace(/\'/g, '"');
			css = (css.indexOf("style=\"") >= 0 ? css.split("style=\"")[1]
					: "\" " + css);
			s = "\
...<div id=\"ID_appletinfotablediv\" style=\"width:Wpx;height:Hpx;position:relative;font-size:14px;text-align:left\">IMG\
......<div id=\"ID_appletdiv\" style=\"z-index:"
					+ J2S.getZ(applet, "header")
					+ (applet._isResizable === false ? ";width:Wpx;height:Hpx;"
							: ";width:100%;height:100%;") +
							"position:absolute;top:0px;left:0px;"
			+ (applet._spinnerImage ? 
					"background-image:url(" + applet._spinnerImage + "); background-repeat:no-repeat; background-position:center;" : "")
					+ css + ">";
			s = s.replace(/IMG/, img).replace(/Hpx/g, applet._containerHeight).replace(/Wpx/g,
					applet._containerWidth);
		} else {
			s = "\
......</div>\
......<div id=\"ID_2dappletdiv\" style=\"position:absolute;width:100%;height:100%;overflow:hidden;display:none\"></div>\
......<div id=\"ID_infotablediv\" style=\"width:100%;height:100%;position:absolute;top:0px;left:0px\">\
.........<div id=\"ID_infoheaderdiv\" style=\"height:20px;width:100%;background:yellow;display:none\"><span id=\"ID_infoheaderspan\"></span><span id=\"ID_infocheckboxspan\" style=\"position:absolute;text-align:right;right:1px;\"><a href=\"javascript:J2S.showInfo(ID,false)\">[x]</a></span></div>\
.........<div id=\"ID_infodiv\" style=\"position:absolute;top:20px;bottom:0px;width:100%;height:100%;overflow:auto\"></div>\
......</div>\
...</div>";
		}
		return s.replace(/\.\.\./g, "").replace(/[\n\r]/g, "").replace(/ID/g,
				applet._id);
	}

	J2S._documentWrite = function(text) {
		if (J2S._document) {
			J2S._document.write(text);
		}
		return text;
	}

	J2S._setObject = function(obj, id, Info) {
		obj._id = id;
		obj.__Info = {};
		Info.z && Info.zIndexBase
				&& (J2S._z = getZOrders(Info.zIndexBase));
		for ( var i in Info)
			obj.__Info[i] = Info[i];
		(obj._z = Info.z) || Info.zIndexBase
				&& (obj._z = obj.__Info.z = getZOrders(Info.zIndexBase));
		obj._width = Info.width;
		obj._height = Info.height;
		obj._isResizable = Info.isResizable;
		obj._noscript = !obj._isJava && Info.noscript;
		obj._console = Info.console;
		obj._cacheFiles = !!Info.cacheFiles;
		obj._viewSet = (Info.viewSet == null || obj._isJava ? null : "Set"
				+ Info.viewSet);
		if (obj._viewSet != null) {
			J2S.View.__init(obj);
			obj._currentView = null;
		}
		if (!obj._console)
			obj._console = obj._id + "_infodiv";
		if (obj._console == "none" || obj._console == "NONE")
			obj._console = null;

		obj._color = (Info.color ? Info.color.replace(/0x/, "#") : "#FFFFFF");
		obj._disableInitialConsole = Info.disableInitialConsole;
		obj._noMonitor = Info.disableJ2SLoadMonitor;
		J2S._j2sPath && (Info.j2sPath = J2S._j2sPath);
		obj._j2sPath = Info.j2sPath;
		obj._coverImage = Info.coverImage;
		obj._isCovered = !!obj._coverImage;
		obj._deferApplet = Info.deferApplet || obj._isCovered && obj._isJava; 
		// must do this if covered in Java
		obj._deferUncover = Info.deferUncover && !obj._isJava; // can't do this
																// with Java
		obj._coverScript = Info.coverScript;
		obj._coverTitle = Info.coverTitle;

		if (!obj._coverTitle)
			obj._coverTitle = (obj._deferApplet ? "activate 3D model"
					: "3D model is loading...")
		obj._containerWidth = obj._width
				+ ((obj._width == parseFloat(obj._width)) ? "px" : "");
		obj._containerHeight = obj._height
				+ ((obj._height == parseFloat(obj._height)) ? "px" : "");
		obj._info = "";
		obj._infoHeader = obj._jmolType + ' "' + obj._id + '"'
		obj._hasOptions = Info.addSelectionOptions;
		obj._defaultModel = Info.defaultModel;
		obj._readyScript = (Info.script ? Info.script : "");
		obj._readyFunction = Info.readyFunction;
		if (obj._coverImage && !obj._deferApplet)
			obj._readyScript += ";javascript " + id
					+ "._displayCoverImage(false)";
		obj._src = Info.src;

	}

	J2S._addDefaultInfo = function(Info, DefaultInfo) {
		for ( var x in DefaultInfo)
			if (typeof Info[x] == "undefined")
				Info[x] = DefaultInfo[x];
		J2S._use && (Info.use = J2S._use);
		if (Info.use.indexOf("SIGNED") >= 0) {
			if (Info.jarFile.indexOf("Signed") < 0)
				Info.jarFile = Info.jarFile.replace(/Applet/, "AppletSigned");
			Info.use = Info.use.replace(/SIGNED/, "JAVA");
			Info.isSigned = true;
		}
	}

	J2S._syncedApplets = [];
	J2S._syncedCommands = [];
	J2S._syncedReady = [];
	J2S._syncReady = false;
	J2S._isJmolJSVSync = false;

	J2S._setReady = function(applet) {
		J2S._syncedReady[applet] = 1;
		var n = 0;
		for (var i = 0; i < J2S._syncedApplets.length; i++) {
			if (J2S._syncedApplets[i] == applet._id) {
				J2S._syncedApplets[i] = applet;
				J2S._syncedReady[i] = 1;
			} else if (!J2S._syncedReady[i]) {
				continue;
			}
			n++;
		}
		if (n != J2S._syncedApplets.length)
			return;
		J2S._setSyncReady();
	}

	J2S._setDestroy = function(applet) {
		// MSIE bug responds to any link click even if it is just a JavaScript
		// call

		if (J2S.featureDetection.allowDestroy)
			J2S.$windowOn('beforeunload', function() {
				J2S._destroy(applet);
			});
	}

	J2S._destroy = function(applet) {
		try {
			if (applet._appletPanel)
				applet._appletPanel.destroy$();
			applet._applet = null;
			J2S.unsetMouse(applet._mouseInterface)
			applet._canvas = null;
			var n = 0;
			for (var i = 0; i < J2S._syncedApplets.length; i++) {
				if (J2S._syncedApplets[i] == applet)
					J2S._syncedApplets[i] = null;
				if (J2S._syncedApplets[i])
					n++;
			}
			if (n > 0)
				return;
			J2S._clearVars();
		} catch (e) {
		}
	}

	// //////////// misc core functionality //////////////

	J2S._setSyncReady = function() {
		J2S._syncReady = true;
		var s = ""
		for (var i = 0; i < J2S._syncedApplets.length; i++)
			if (J2S._syncedCommands[i])
				s += "J2S.script(J2S._syncedApplets[" + i
						+ "], J2S._syncedCommands[" + i + "]);"
		setTimeout(s, 50);
	}

	J2S._mySyncCallback = function(appFullName, msg) {
		app = J2S._applets[appFullName];
		if (app._viewSet) {
			// when can we do this?
			// if (app._viewType == "JSV" && !app._currentView.JMOL)
			J2S.View.updateFromSync(app, msg);
			return;
		}
		if (!J2S._syncReady || !J2S._isJmolJSVSync)
			return 1; // continue processing and ignore me
		for (var i = 0; i < J2S._syncedApplets.length; i++) {
			if (msg.indexOf(J2S._syncedApplets[i]._syncKeyword) >= 0)
				J2S._syncedApplets[i]._syncScript(msg);
		}
		return 0 // prevents further Jmol sync processing
	}

	J2S._getElement = function(applet, what) {
		var d = document.getElementById(applet._id + "_" + what);
		return (d || {});
	}

	J2S._evalJSON = function(s, key) {
		s = s + "";
		if (!s)
			return [];
		if (s.charAt(0) != "{") {
			if (s.indexOf(" | ") >= 0)
				s = s.replace(/\ \|\ /g, "\n");
			return s;
		}
		var A = (new Function("return " + s))();
		return (!A ? null : key && A[key] != undefined ? A[key] : A);
	}

	J2S._sortMessages = function(A) {
		/*
		 * private function
		 */
		function _sortKey0(a, b) {
			return (a[0] < b[0] ? 1 : a[0] > b[0] ? -1 : 0);
		}

		if (!A || typeof (A) != "object")
			return [];
		var B = [];
		for (var i = A.length - 1; i >= 0; i--)
			for (var j = 0, jj = A[i].length; j < jj; j++)
				B[B.length] = A[i][j];
		if (B.length == 0)
			return;
		B = B.sort(_sortKey0);
		return B;
	}

	// ////////////////// mouse and key events //////////////////////

	var doIgnore = function(ev,test) {
		var ignore = (
				J2S._dmouseOwner && J2S._dmouseOwner.className == "swingjs-resizer"
				|| ev.originalEvent.xhandled 
				|| !ev.target 
				|| ("" + ev.target.className).indexOf("swingjs-ui") >= 0
			);
		if (!test)
			ev.originalEvent.xhandled = true;
		return ignore;
	};

	J2S.setKeyListener = function(who) {
		J2S.$bind(who, 'keydown keypress keyup', function(ev) {
			if (doIgnore(ev))
				return true;
			if (ev.target.getAttribute("role")) {
				// TODO -- check this
				return true;
			}
			var target = ev.target["data-keycomponent"];
// BH 2019 - need this for the focus manager to pick up accelerators and other mapped items
//if (!target) {
//	  return;
//}
if (ev.keyCode == 9 && ev.target["data-focuscomponent"]) {
	ev.stopPropagation();
	ev.preventDefault();
}

			var id;
			switch (ev.type) {
			case "keypress":
				id = 400;
				break;
			case "keydown":
				id = 401;
				break;
			case "keyup":
				id = 402;
				break;
			}
			who.applet._processEvent(id, [0,0,getKeyModifiers(ev)], ev, who._frameViewer);
			return ev.originalEvent.xallowKeyEvent || !!(target);
		});
	}
	
	// set to ignore touches if a mouse is found. Will break gestures on touch-screen laptops, but 
	// it enables click in touch-only devices. What a pain!
	
	J2S._haveMouse;
	J2S._firstTouch; // three-position switch: undefined, true, false

	J2S.$bind('body', //'pointerdown pointermove 
		'mousedown mousemove mouseup', function(ev) {
		J2S._haveMouse = true;
	});
	
	J2S.$bind('body', //'pointerup 
		'mouseup touchend', function(ev) {
		mouseUp(null, ev);
		return true;
	});

	J2S.traceMouse = function(who,what,ev) {
		System.out.println(["tracemouse:" + what 
			,"type:",ev.type,ev.pageX,ev.pageY
			,"target.id:",ev.target.id
			,"\n  relatedtarget.id:",(ev.originalEvent.relatedTarget && ev.originalEvent.relatedTarget.id)
			,"\n  who:", who.id
			,"\n  dragging:", (J2S._mouseOwner && J2S._mouseOwner.isDragging)
			,"doignore:",doIgnore(ev,1)
			,"role:",ev.target.getAttribute && ev.target.getAttribute("role")
			,"data-ui:",ev.target["data-ui"]
			,"data-component:",ev.target["data-component"]
			,"mouseOwner:",J2S._mouseOwner && J2S._mouseOwner.id
		].join().replace(":,",":"));
	}

	var checkStopPropagation = function(ev, ui, handled, target) {
		if (ui && ui.checkStopPropagation$O$Z) {
			handled = ui.checkStopPropagation$O$Z(ev, handled);
		} else if (!ui || !handled || !ev.target.getAttribute("role")) {
			if (!target || !target.ui.buttonListener) {					
				ev.preventDefault();
				ev.stopPropagation();
			}
		}
		// handled -- we are done here
		return handled;
	};

	var mouseEnter = function(who, ev) {
		if (who.applet == null)
			return;
		if (J2S._traceMouse)
			J2S.traceMouse(who,"ENTER", ev);

		if (doIgnore(ev))
			return true;
		if (ev.target.getAttribute("role")) {
			return true;
		}
		if (J2S._mouseOwner && !J2S._mouseOwner.isDragging)
			J2S.setMouseOwner(null);
		var xym = getXY(who, ev, 0);
		if (!xym)
			return false;
		who.applet._processEvent(504, xym, ev, who._frameViewer);// MouseEvent.MOUSE_ENTERED
		return false;
	}

	var mouseDown = function(who, ev) {
		// prevent touch dragging
		if (who.applet == null)
			return;
		if (J2S._traceMouse)
			J2S.traceMouse(who,"DOWN", ev);

		// If we have a mousedown on the applet, then disable touch; 
		// otherwise, if J2S._firstTouch is undefined (!!x != x), set J2S._firstTouch
		// and ignore future touch events (through the first touchend):
		
		if (//ev.type == "pointerdown" || 
			ev.type == "mousedown") {// BHTEst
		    J2S._haveMouse = true;
		} else { 
		    if (J2S._haveMouse) return;
		    if (!!J2S._firstTouch != J2S._firstTouch) {
// q - why did we do this?
//			J2S._firstTouch = true;
//		        return;
			J2S._firstTouch = false;
		    }
		}

		lastDragx = lastDragy = 99999;

		if (doIgnore(ev))
			return true;

		J2S.setMouseOwner(who, true, ev.target);
		var ui = ev.target["data-ui"];
		var target = ev.target["data-component"];
		var handled = (ui && ui.handleJSEvent$O$I$O(who, 501, ev));
		if (checkStopPropagation(ev, ui, handled, target))
			return true;
		who.isDragging = true;
		if ((ev.type == "touchstart") && J2S._gestureUpdate(who, ev))
			return !!target;
		J2S._setConsoleDiv(who.applet._console);
		var xym = getXY(who, ev, 0);
		if (xym) {
			if (ev.button != 2 && J2S.Swing && J2S.Swing.hideMenus)
				J2S.Swing.hideMenus(who.applet);
//			if (who._frameViewer && who._frameViewer.isFrame)
//				J2S.setWindowZIndex(who._frameViewer.top.ui.domNode,
//						Integer.MAX_VALUE);
			who.applet._processEvent(501, xym, ev, who._frameViewer); // MouseEvent.MOUSE_PRESSED
			
			
			who.isDown = true;
		}

		return !!(ui || target);
//		return !!target || ui && ui.j2sDoPropagate;

	}
	
	var mouseMove = function(who, ev) {
		// ignore touchmove if J2S._haveMouse
		
		if (who.applet == null)
			return;

		if (ev.type == "touchmove" && 
				(J2S._firstTouch || J2S._haveMouse)) {
			return;
		}
		
		if (J2S._dmouseOwner && J2S._dmouseOwner.isDragging) {
		    // resizing mouse dragged over applet
			if (J2S._dmouseDrag)
				J2S._dmouseDrag(ev);
			else
				J2S._dmouseOwner = null;
		}
		
		if (J2S._traceMouseMove)
			J2S.traceMouse(who, "MOVE", ev);

		if (doIgnore(ev))
			return true;

		if (ev.target.getAttribute("role")) {
			return true;
		}

		// defer to console or menu when dragging within this who

		if (J2S._mouseOwner && J2S._mouseOwner != who
				&& J2S._mouseOwner.isDragging) {
			if (!J2S._mouseOwner.mouseMove)
				return true;
			J2S._mouseOwner.mouseMove(ev);
			return false;
		}
		return J2S._drag(who, ev, 503);
	}
	
	var mouseUp = function(who, ev) {
		if (J2S._dmouseOwner && J2S._dmouseOwner.isDragging) {
		    // resizing mouse released over applet
			if (J2S._dmouseDrag) {
				J2S._dmouseUp(ev);
			} else {
				System.out.println("move setting dmouseowner null");
				J2S._dmouseOwner = null;
			}
		}
		if (!who || who.applet == null)
			return;
		who.isDown = false;
		if (J2S._traceMouse)
			J2S.traceMouse(who,"UP", ev);

		// If we have a touchend, ignore it if we have found a mouse or it is a first touch, 
		// and set J2S.firstTouch false:
			
		if (ev.type == "touchend") {
		    if (J2S._haveMouse) return;
		    if (J2S._firstTouch) {
		    	J2S._firstTouch = false;
		        return;
		    }
		}

		if (doIgnore(ev))
			return true;

		if (J2S._mouseOwner)
			who = J2S._mouseOwner;

		J2S.setMouseOwner(null);

		if (!who)
			return true;
		
		var ui = ev.target["data-ui"]; // e.g., a textbox
		var target = ev.target["data-component"]; // e.g., a button
		var handled = (ui && ui.handleJSEvent$O$I$O(who, 502, ev));
		if (checkStopPropagation(ev, ui, handled))
			return true;
		
		who.isDragging = false;
		
		if (ev.type != "touchend" || !J2S._gestureUpdate(who, ev)) {
			var xym = getXY(who, ev, 502);
			if (xym)
				who.applet._processEvent(502, xym, ev, who._frameViewer);// MouseEvent.MOUSE_RELEASED
		}
					
		return !!(ui || target);
	}
	
	var mouseClick = function(who, ev) {

		if (who.applet == null) {
			who.isDown = false;
			return;
		}
		
		if (who.isDown) {
		  // this may happen with a pointer. In this case
		  // we have to generate the down first. 
			ev.type = "pointerup";
			mouseUp(who, ev);
			ev.type = "click";
			ev.originalEvent.xhandled = false;
		}

		if (J2S._traceMouse)
			J2S.traceMouse(who,"CLICK", ev);

		if (doIgnore(ev))
			return true;
		if (ev.target.getAttribute("role")) {
			return true;
		}

		J2S.setMouseOwner(null);
		var xym = getXY(who, ev, 0);
		if (!xym)
			return false;
		who.applet._processEvent(500, xym, ev, who._frameViewer);// MouseEvent.MOUSE_CLICK
		return true; // was false
	}
	
	var mouseWheel = function(who, ev) {
		
		if (who.applet == null) {
			return;
		}
		// Zoom
			// not for wheel event, or action will not take place on handle and
			// track
			// if (doIgnore(ev))
			// return true;

			if (J2S._traceMouse)
				J2S.traceMouse(who,"SCROLL", ev);

			if (ev.target.getAttribute("role")) {
				return true;
			}
			var ui = ev.target["data-ui"];
			var target = ev.target["data-component"];
			var handled = (ui && ui.handleJSEvent$O$I$O(who, 507, ev));
			if (checkStopPropagation(ev, ui, handled))
				return true;
			who.isDragging = false;

			var oe = ev.originalEvent;
			var scroll = (oe.detail ? oe.detail
					: (J2S.featureDetection.os == "mac" ? 1 : -1)
							* oe.wheelDelta); // Mac and PC are reverse; but
			var xym = getXY(who, ev, 0);

			if (xym) {
				xym.push(scroll < 0 ? -1 : 1)
				who.applet._processEvent(507, xym, ev, who._frameViewer);
			}
			return !!(ui || target);

	}
	
	var mouseLeave = function(who, ev) {
		if (who.applet == null) {
			return;
		}
		if (J2S._traceMouse)
			J2S.traceMouse(who,"OUT", ev);

		if (doIgnore(ev))
			return true;
		if (ev.target.getAttribute("role")) {
			return true;
		}
		
		if (J2S._mouseOwner && !J2S._mouseOwner.isDragging)
			J2S.setMouseOwner(null);
		if (who.applet._appletPanel)
			who.applet._appletPanel.startHoverWatcher$Z(false);
		var xym = getXY(who, ev, 0);
		if (!xym)
			return false;
		who.applet._processEvent(505, xym, ev, who._frameViewer);// MouseEvent.MOUSE_EXITED
		return false;
	}
	
	var mouseMoveOut = function(who, ev) {
		if (!who.isDragging || who != J2S._mouseOwner)
			return;
		if (J2S._traceMouse)
			J2S.traceMouse(who,"OUTJSMOL", ev);
		return J2S._drag(who, ev, 503);
	}
	
	var mouseUpOut = function(who, ev) {
		if (!who.isDragging || who != J2S._mouseOwner)
			return true;
		if (J2S._traceMouse)
			J2S.traceMouse(who,"UPJSMOL", ev);
		return J2S._drag(who, ev, 502);
	}
	
	J2S.setMouse = function(who, isSwingJS) {
		// swingjs.api.J2SInterface


		J2S.$bind(who, (J2S._haveMouse ? 'mousemove pointermove' : 'pointermove mousemove touchmove'), 
				function(ev) { return mouseMove(who, ev) });

		J2S.$bind(who, 'click', function(ev) { return mouseClick(who, ev) });
		
		J2S.$bind(who, 'DOMMouseScroll mousewheel', function(ev) { return mouseWheel(who, ev) });

		J2S.$bind(who, (J2S._haveMouse ? 'mousedown pointerdown' : 'pointerdown mousedown touchstart'), 
				function(ev) { return mouseDown(who, ev) });

		J2S.$bind(who, (J2S._haveMouse ? 'mouseup pointerup' : // 'pointerup 
		'mouseup touchend'), 
				function(ev) { return mouseUp(who, ev) });

		J2S.$bind(who, 'pointerenter mouseenter', function(ev) { return mouseEnter(who, ev) });

		J2S.$bind(who, 'pointerout mouseleave', function(ev) { return mouseLeave(who, ev) });

		// context menu is fired on mouse down, not up, and it's handled already
		// anyway.

		J2S.$bind(who, "contextmenu", function() { return false });

		J2S.$bind(who, 'mousemoveoutjsmol', 
				function(evspecial, target, ev) { return mouseMoveOut(who, ev) });

		J2S.$bind(who, 'mouseupoutjsmol', 
				function(evspecial, target, ev) { return mouseUpOut(who, ev) });

		if (who.applet && who.applet._is2D && !who.applet._isApp) {
			J2S.$resize(function() {
				if (!who.applet)
					return;
				who.applet._resize();
			});
		}
		$(who).css({"touch-action":"none"}); // disable browser panning upon touch-drag
	}

	J2S.unsetMouse = function(who) {
		if (!who)
			return;
		// swingjs.api.J2SInterface
		who.applet = null;
		who._frameViewer = null;
		J2S.$bind(who,
				'mouseupoutjsmol click touchoutjsmol pointerupoutjsmol '
				+'mousedown pointerdown touchstart '
				+'mousemove touchmove pointermove ' 
				+'mouseup pointerup touchend '
				+'DOMMouseScroll mousewheel contextmenu '
				+'mouseleave mouseenter mousemoveoutjsmol '
				+'pointerout pointerenter pointermoveoutjsmol ',
				null);
		J2S.setMouseOwner(null);
	}

	J2S.setMouseOwner = function(who, doSet, target) {
		// called for mousedown, mouseup, mouse, jsUnsetMouse, 
		// and outsideEvent.teardown, outsideEvent.mouseUp
		if (!who && J2S._mouseOwner)
			J2S._mouseOwner.isDragging = false;

		//who && who.focus();

		if (!who || doSet) {
			J2S._mouseOwner = who;
			who && who.applet && (J2S._lastAppletID = who.applet._id);			
		} else if (J2S._mouseOwner == who) {
			J2S._mouseOwner = who = null;
		} 
		if (target || !who)
			J2S._mouseTarget = target || null;
	}

	J2S._drag = function(who, ev, id) {

		if (id != 503) {
			ev.stopPropagation();
			ev.preventDefault();
		}

		var newid = (id == 502 ? 502 : J2S._mouseOwner && J2S._mouseOwner.isDragging ? 506 : 503);
		// MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED

		var isTouch = (ev.type == "touchmove");
		if (isTouch) {
			if (J2S._gestureUpdate(who, ev))
				return false;
			if (newid == 506) {
				ev.button = ev.originalEvent.button = 0;
				ev.buttons = ev.originalEvent.buttons = 1;
			}
		}
		var xym = getXY(who, ev, id);
		if (!xym)
			return false;

		if (id != 502 && lastDragx == xym[0] && lastDragy == xym[1])
			return false;
		lastDragx = xym[0];
		lastDragy = xym[1];


		var ui = ev.target["data-ui"];
		var target = ev.target["data-component"];

		who.applet._processEvent(newid, xym, ev, who._frameViewer);
		return !!(ui || target);
	}

	var getMouseModifiers = function(ev, id) {
		// id needed to properly not assign the InputEvent.ButtonX_DOWN_MASK for an UP operation
		// and also recognize a drag (503 + buttons pressed
		var modifiers = 0;
		if (id == 503) {
			modifiers = (ev.buttons == 0 ? 0 : ev.buttons == 2 ? (1 << 12) : (1 << 10));
		} else {
			switch (ev.button) {
			default:
				ev.button = 0;
				// fall through
			case 0:
				modifiers = (1 << 4) | (id ? 0 : (1 << 10));// InputEvent.BUTTON1 +					
															// InputEvent.BUTTON1_DOWN_MASK;
				
				break;
			case 1:
				modifiers = (1 << 3) | (id ? 0 : (1 << 11));// InputEvent.BUTTON2 +
															// InputEvent.BUTTON2_DOWN_MASK;
				break;
			case 2:
				modifiers = (1 << 2) | (id ? 0 : (1 << 12));// InputEvent.BUTTON3 +
															// InputEvent.BUTTON3_DOWN_MASK;
				break;
			}
		}
		return modifiers | getKeyModifiers(ev);
	}

	var getKeyModifiers = function(ev) {
		var modifiers = 0;
		if (ev.shiftKey)
			modifiers |= (1 << 0) | (1 << 6); // InputEvent.SHIFT_MASK +
												// InputEvent.SHIFT_DOWN_MASK;
		if (ev.ctrlKey)
			modifiers |= (1 << 1) | (1 << 7); // InputEvent.CTRL_MASK +
												// InputEvent.CTRL_DOWN_MASK;
		if (ev.metaKey)
			modifiers |= (1 << 2) | (1 << 8); // InputEvent.META_MASK +
												// InputEvent.META_DOWN_MASK;
		if (ev.altKey)
			modifiers |= (1 << 3) | (1 << 9); // InputEvent.ALT_MASK +
												// InputEvent.ALT_DOWN_MASK;
		if (ev.altGraphKey)
			modifiers |= (1 << 5) | (1 << 13); // InputEvent.ALT_GRAPH_MASK +
												// InputEvent.ALT_GRAPH_DOWN_MASK;
		return modifiers;
	}

	var getXY = function(who, ev, id) {
		// id 0, 502, or 503 only 
		if (!who.applet._ready || J2S._touching && ev.type.indexOf("touch") < 0)
			return false;
		// text-box clicking in SwingJS
		if (ev.target == who) {
			var ui = ev.target["data-ui"];
			if (ui) {
				var top = ui.jc.getTopLevelAncestor$();
				if (top)
					who = top.ui.domNode;
				// else we have a popup menu	
			}
		}
		var offsets = J2S.$offset(who.id);
		if (!offsets) {
			// someone forgot to remove the event handlers for an object removed from the DOM
			J2S.unsetMouse(who);
			return;
		}
		return J2S._getEventXY(ev, offsets, getMouseModifiers(ev, id));
	}

	J2S._getEventXY = function(ev, offsets, mods) {
		var x, y;
		var oe = ev.originalEvent;
		// drag-drop jQuery event is missing pageX
		oe.targetTouches && (oe = oe.targetTouches[0]);
		ev.pageX || (ev.pageX = oe ? oe.pageX : J2S._mousePageX);
		ev.pageY || (ev.pageY = oe ? oe.pageY : J2S._mousePageY);
		x = J2S._mousePageX = Math.round(ev.pageX);
		y = J2S._mousePageY = Math.round(ev.pageY);
		return [ Math.round(x - offsets.left), Math.round(y - offsets.top), mods];
	}
	
	J2S._gestureUpdate = function(who, ev) {
		var oe = ev.originalEvent;
		switch (ev.type) {
		case "touchstart":
			J2S._touching = true;
			break;
		case "touchend":
			J2S._touching = false;
			break;
		}
		if (!oe.touches || oe.touches.length != (ev.type == "touchend" ? 1 : 2))
			return false;
		var n = 0;
		switch (ev.type) {
		case "touchstart":
			who._touches = [ [], [] ];
			break;
		case "touchend":
		case "touchmove":
			var offsets = J2S.$offset(who.id);
			var t0 = who._touches[0];
			var t1 = who._touches[1];
			t0.push([ oe.touches[0].pageX - offsets.left,
					oe.touches[0].pageY - offsets.top ]);
			if (ev.type != "touchend")
			    t1.push([ oe.touches[1].pageX - offsets.left,
					oe.touches[1].pageY - offsets.top ]);
			n = t0.length;
			if (n > 3) {
				t0.shift();
				t1.shift();
			}
			if (n >= 2)
				who.applet._processGesture(who._touches, who._frameViewer);
			break;
		}
		ev.stopPropagation();
		ev.preventDefault();
		return true;
	}

	var lastDragx = 99999;
	var lastDragy = 99999;

	J2S.getMousePosition = function(p) {
		p.x = lastDragx;
		p.y = lastDragy;
		return p;
	}
	
	J2S._track = function(applet) {
		// this function inserts an iFrame that can be used to track your page's
		// applet use.
		// By default it tracks to a page at St. Olaf College, but you can
		// change that.
		// and you can use
		//
		// delete J2S._tracker
		//
		// yourself to not have you page execute this
		//
		if (J2S._tracker) {
			try {
				var url = J2S._tracker + "&applet=" + applet._jmolType
						+ "&version=" + J2S._version + "&appver="
						+ J2S.___JmolVersion + "&url="
						+ encodeURIComponent(document.location.href);
				var s = '<iframe style="display:none" width="0" height="0" frameborder="0" tabindex="-1" src="'
						+ url + '"></iframe>'
				J2S.$after("body", s);
			} catch (e) {
				// ignore
			}
			delete J2S._tracker;
		}
		return applet;
	}

	var __profiling;

	J2S.getProfile = function(doProfile) {
		if (__profiling || arguments.length == 1 && !doProfile) {
			var s = Clazz.getProfile();
			System.out.println(s);
			alert(s);
			return;
		} 
		var seconds = (arguments[0] === true ? 0 : +(arguments.length == 0 ? prompt("How many seconds?", "0 (until I click again)") : arguments[0]));
		if (isNaN(seconds))
			seconds = 0;
		
		Clazz.startProfiling(__profiling = (seconds || arguments.length == 0 || doProfile));
	}

	J2S._getAttr = function(s, a) {
		var pt = s.indexOf(a + "=");
		return (pt >= 0 && (pt = s.indexOf('"', pt)) >= 0 ? s.substring(pt + 1,
				s.indexOf('"', pt + 1)) : null);
	}

	J2S.Cache = {
		fileCache : {}
	};

	J2S.Cache.get = function(filename) {
		return J2S.Cache.fileCache[filename];
	}

	J2S.Cache.put = function(filename, data) {
		J2S.Cache.fileCache[filename] = data;
	}
	// dnd _setDragDrop for swingjs.api.J2S called JSComponentUI
	J2S.setDragDropTarget = J2S.Cache.setDragDrop = function(me, node, adding) {
		if (adding === false) {
			node["data-dropComponent"] = null;
			J2S.$appEvent(node, null, "dragover", null);
			J2S.$appEvent(node, null, "drop", null);
			return;
		}
		if (adding === true) {
			node["data-dropComponent"] = me;
			me = node;
			node = null;
		}
		// me can be the node if node is null
		node || (node = null);

		
		J2S.$appEvent(me, node, "dragover", function(e) { 
			e = e.originalEvent;
			e.stopPropagation();
			e.preventDefault();
			if (e.target == J2S._mouseOwner) {
				return; // for now
				e.dataTransfer.dropEffect = 'move';				
			} else {
				e.dataTransfer.dropEffect = 'copy';				
			}
		});
		J2S.$appEvent(me, node, "drop", function(e) {
			J2S._mouseOwner && (J2S._mouseOwner.isDragging = false);
			var oe = e.originalEvent;
			if (e.target == J2S._mouseOwner) {
				oe.preventDefault();
				oe.stopPropagation();
				return; // for now
			}
			if (!oe.dataTransfer)
				return;
			try {
				var kind = oe.dataTransfer.items[0].kind;
				var type = oe.dataTransfer.items[0].type;
				var file = oe.dataTransfer.files[0];
				var files = oe.dataTransfer.files;
			} catch (e) {
				return;
			} finally {
				oe.preventDefault();
				var doStop = (e.target != J2S._mouseOwner)
				if (doStop) {				
					oe.stopPropagation();
				}
			}
			var target = oe.target;
			var c = target;
			var comp;
			while (c && !(comp = c["data-dropComponent"]))
				c = c.parentElement;
			if (!comp)
				return;
			var d = comp.getLocationOnScreen$();
			var x = oe.pageX - d.x;
			var y = oe.pageY - d.y;
			if (file == null) {
				// FF and Chrome will drop an image here
				// but it will be only a URL, not an actual file.

				
				Clazz.loadClass("swingjs.JSDnD")
						.drop$javax_swing_JComponent$O$S$BA$I$I(comp,
								oe.dataTransfer, null, null, x, y);
				return;
			}
			// MSIE will drop an image this way, though, and load it!
			var nfiles = files.length;
			var arr = [];
			for (var i = 0; i < nfiles; i++) {
				var file = files[i];
				var reader = new FileReader();
				reader.onloadend = function(evt) {
					if (evt.target.readyState == FileReader.DONE) {
						var target = oe.target;
						var name = evt.target._filename;
						var bytes = J2S._toBytes(evt.target.result);
						arr.push([name, bytes]);
						System.out.println("j2sApplet DnD kind=" + kind + " type=" + type + " name=" + name + " size="+ bytes.length);
						if (--nfiles == 0) {
						  Clazz.loadClass("swingjs.JSDnD")
								.drop$javax_swing_JComponent$O$OAA$I$I(comp, oe.dataTransfer, arr, x, y);
						}
					}
				};
				reader._filename = file.name
				reader.readAsArrayBuffer(file);
			}
		});
	}

	J2S._isAsync = false; // testing only
	J2S._asyncCallbacks = {};

	J2S._coreFiles = []; // required for package.js

	// /////////////////
	// This section provides an asynchronous loading sequence
	//

	// methods and fields starting with double underscore are private to this
	// .js file

	var __clazzLoaded = false;
	var __execLog = [];
	var __execStack = [];
	var __execTimer = 0;
	var __coreSet = [];
	var __coreMore = [];
	var __execDelayMS = 100; // must be > 55 ms for FF

	var __nextExecution = function(trigger) {
		arguments.length || (trigger = true);
		delete __execTimer;
		var es = __execStack;
		var e;
		while (es.length > 0 && (e = es[0])[4] == "done")
			es.shift();
		if (es.length == 0)
			return;
		if (!J2S._isAsync && !trigger) {
			setTimeout(__nextExecution, 10)
			return;
		}
		e.push("done");
		var s = "j2sApplet exec " + e[0]._id + " " + e[3] + " " + e[2];
		if (self.System)
			System.out.println(s);
		// alert(s)
		if (window.console)
			window.console.log(s + " -- OK")
		__execLog.push(s);
		e[1](e[0], e[2]);
	};

	var __loadClazz = function(applet) {
		if (!__clazzLoaded) {
			__clazzLoaded = true;
			// create the Clazz object
			J2S.LoadClazz(Clazz);
			if (J2S._strict)
				System.err.println("j2sApplet j2sstrict - 'use strict' will be used - this is experimental");
			if (J2S._startProfiling) 
				J2S.getProfile();
			if (applet._noMonitor)
				Clazz._LoaderProgressMonitor.showStatus = function() {
				}
			J2S.LoadClazz = null;
			if (applet.__Info.uncompressed)
				Clazz.loadClass(); // for now; allows for no compression
			Clazz._Loader.onGlobalLoaded = function(file) {
				// not really.... just nothing more yet to do yet
				Clazz._LoaderProgressMonitor.showStatus("Application loaded.",
						true);
				if (!J2S._debugCode || !J2S.haveCore) {
					J2S.haveCore = true;
					__nextExecution();
				}
			};
			// load package.js and j2s/core/core.z.js
			Clazz._Loader.loadPackageClasspath("java", null, true,
					__nextExecution);
			return;
		}
		__nextExecution();
	};
	
	J2S.showStatus = function(msg, doFadeout) {
		Clazz._LoaderProgressMonitor.showStatus(msg, doFadeout);
	}

	J2S.debugClip = function() { return J2S._debugClip };
	
	var __loadClass = function(applet, javaClass) {
		Clazz._Loader.loadClass(javaClass, function() {
			__nextExecution()
		});
	};

	J2S.showExecLog = function() {
		return __execLog.join("\n")
	};

	J2S._addExec = function(e) {
		e[1] || (e[1] = __loadClass);
		var s = "J2SApplet load " + e[0]._id + " " + e[3];
		if (self.console)
			console.log(s + "...")
		__execLog.push(s);
		__execStack.push(e);
	}

	J2S._addCoreFile = function(type, path, more) {

		// BH 3/15: idea here is that when both Jmol and JSV are present,
		// we want to load a common core file -- jmoljsv.z.js --
		// instead of just one. Otherwise we do a lot of duplication.
		// It is not clear how this would play with other concurrent
		// apps. So this will take some thinking. But the basic idea is that
		// core file to load is
		
		if (Array.isArray(type)) {
			more = type;
			type = null;
		}

		if (type) {
			type = type.toLowerCase().split(".")[0]; // package name only

			// return if type is already part of the set.
			if (__coreSet.join("").indexOf(type) >= 0)
				return;

			// create a concatenated lower-case name for a core file that
			// includes
			// all Java applets on the page

			__coreSet.push(type);
			__coreSet.sort();
			J2S._coreFiles = [ path + "/core/core" + __coreSet.join("")
					+ ".z.js" ];
		}
		if (more && (Array.isArray(more) || (more = more.split(" "))))
			for (var i = 0; i < more.length; i++)
				if (more[i] && __coreMore.join("").indexOf(more[i]) < 0)
					__coreMore.push(path + "/core/core" + more[i] + ".z.js")
		for (var i = 0; i < __coreMore.length; i++)
			J2S._coreFiles.push(__coreMore[i]);
	}

	J2S._Canvas2D = function(id, Info, type, checkOnly) {
		// type: Jmol or JSV or SwingJS
		this._uniqueId = ("" + Math.random()).substring(3);
		this._id = id;
		this._is2D = true;
		this._isJava = false;
		this._isJNLP = !!Info.main;
		if (typeof Info.isResizable == "undefined")
			Info.isResizable = (("" + Info.width).indexOf("px")< 0);
		this._jmolType = "J2S._Canvas2D (" + type + ")";
		this._isLayered = Info._isLayered || false; // JSV or SwingJS are
													// layered
		this._isSwing = Info._isSwing || false;
		this._isApp = !!Info._main;
		this._isJSV = Info._isJSV || false;
		this._isAstex = Info._isAstex || false;
		this._platform = Info._platform || "";
		this._spinnerImage = (!Info.spinnerImage || Info.spinnerImage == "NONE" || Info.spinnerImage == "none" ? null 
				: Info.spinnerImage.indexOf("//") < 0 && Info.spinnerImage.indexOf("/") != 0 ? Info.j2sPath + "/" + Info.spinnerImage 
				: Info.spinnerImage);
		if (checkOnly)
			return this;
		J2S.setWindowVar(id, this);
		if (!this._isApp)
			this._createCanvas(id, Info);
		if (!this._isJNLP && (!J2S._document || this._deferApplet))
			return this;
		this._init();
		return this;
	};

	J2S._setAppletParams = function(availableParams, params, Info, isHashtable) {
		for (var i in Info) {
			var lci = i.toLowerCase();
			if (!availableParams
					|| availableParams.indexOf(";" + lci + ";") >= 0) {
				if (Info[i] == null || lci == "language"
						&& !J2S.featureDetection.supportsLocalization())
					continue;
				if (isHashtable)
					params.put$O$O(i, (Info[i] === true ? Boolean.TRUE
							: Info[i] === false ? Boolean.FALSE : Info[i]))
				else
					params[i] = Info[i];
			}
		}
	}

	// The original Jmol "applet" was created as an 
	// extension to a canvas. We still do that even
	// though it doesn't make a lot of sense. Nonetheless,
	// this canvas is used for the main canvas for 
	// a SwingJS applet.
	J2S._jsSetPrototype = function(proto) {
		proto._init = function() {
			this._setupJS();
			this._showInfo(!this.__Info.console);
			if (this._disableInitialConsole)
				this._showInfo(false);
		};

		proto._createCanvas = function(id, Info) {
			J2S._setObject(this, id, Info);
			if (Info.main) // a Java application, not an applet -- let
							// AppletViewer take care of this
				return;
			var t = J2S._getWrapper(this, true);
			if (this._deferApplet) {
			} else if (J2S._document) {
				J2S._documentWrite(t);
				this._newCanvas(false);
				t = "";
			} else {
				this._deferApplet = true;
				t += '<script type="text/javascript">' + id
						+ '._cover(false)</script>';
			}
			t += J2S._getWrapper(this, false);
			if (Info.addSelectionOptions)
				t += J2S._getGrabberOptions(this);
			if (J2S._debugAlert && !J2S._document)
				alert(t);
			this._code = J2S._documentWrite(t);
		};

		proto._newCanvas = function(doReplace) {
			if (this._is2D)
				this._createCanvas2d(doReplace);
			else
				this._GLmol.create();
		};

		// ////// swingjs.api.HTML5Applet interface
		proto._getHtml5Canvas = function() {
			return this._canvas
		};
				
		proto._setAppClass = function(app) { this.getApp = function() {this._setThread();return app}};
		
		proto._getWidth = function() {
			return (this._canvas ? this._canvas.width : 0)
		};
		proto._getHeight = function() {
			return (this._canvas ? this._canvas.height : 0)
		};
		proto._getContentLayer = function() {
			return J2S.$(this, "contentLayer")[0]
		};
		proto.repaintNow = function() {
			J2S.repaint(this, false)
		};
		// //////

		proto._setThread = function() { swingjs.JSToolkit.getCurrentThread$javajs_util_JSThread(this._appletPanel.appletViewer.myThread)}
		proto._createCanvas2d = function(doReplace) {
			var container = J2S.$(this, "appletdiv");
			// if (doReplace) {

			if (this._canvas) {
				try {
					container[0].removeChild(this._canvas);
					if (this._canvas.frontLayer)
						container[0].removeChild(this._canvas.frontLayer);
					if (this._canvas.rearLayer)
						container[0].removeChild(this._canvas.rearLayer);
					if (this._canvas.contentLayer)
						container[0].removeChild(this._canvas.contentLayer);
					J2S.unsetMouse(this._mouseInterface);
				} catch (e) {
				}
			}
			var w = Math.round(container.width());
			var h = Math.round(container.height());
			var canvas = document.createElement('canvas');
			canvas.applet = this;
			this._canvas = canvas;
			canvas.style.width = "100%";
			canvas.style.height = "100%";
			canvas.width = w;
			canvas.height = h; // w and h used in setScreenDimension
			canvas.id = this._id + "_canvas2d";
			container.append(canvas);
			J2S._$(canvas.id).css({
				"z-index" : J2S.getZ(this, "main")
			});
			if (this._isLayered) {
				var content = document.createElement("div");
				canvas.contentLayer = content;
				content.id = this._id + "_contentLayer";
				container.append(content);
				J2S._$(content.id).css({
					zIndex : J2S.getZ(this, "content"),
					position : "absolute",
					left : "0px",
					top : "0px",
					width : (this._isSwing ? w : 0) + "px",
					height : (this._isSwing ? h : 0) + "px",
					overflow : "hidden"
				});
				if (this._isSwing) {
					this._mouseInterface = content;
					content.applet = this;
				} else {
					this._mouseInterface = this._getLayer("front", container,
							w, h, false);
				}
			} else {
				this._mouseInterface = canvas;
			}
			J2S.setMouse(this._mouseInterface, this._isSwing);
		}

		proto._getLayer = function(name, container, w, h, isOpaque) {
			var c = document.createElement("canvas");
			this._canvas[name + "Layer"] = c;
			c.style.width = "100%";
			c.style.height = "100%";
			c.id = this._id + "_" + name + "Layer";
			c.width = w;
			c.height = h; // w and h used in setScreenDimension
			container.append(c);
			c.applet = this;
			J2S._$(c.id).css({
				background : (isOpaque ? "rgb(0,0,0,1)" : "rgb(0,0,0,0.001)"),
				"z-index" : J2S.getZ(this, name),
				position : "absolute",
				left : "0px",
				top : "0px",
				overflow : "hidden"
			});
			return c;
		}

		proto._setupJS = function() {
			J2S.setGlobal("j2s.lib", {
				base : this._j2sPath + "/",
				alias : ".",
				console : this._console,
				monitorZIndex : J2S.getZ(this, "monitorZIndex")
			});
			J2S.setGlobal("j2s.tmpdir", "/TEMP/");
			var isFirst = (__execStack.length == 0);
			if (isFirst)
				J2S._addExec([ this, __loadClazz, null, "loadClazz" ]);
			this._addCoreFiles();
			J2S._addExec([ this, this.__startAppletJS, null, "start applet" ])
			this._isSigned = true; // access all files via URL hook
			this._ready = false;
			this._applet = null;
			this._canScript = function(script) {
				return true;
			};
			this._savedOrientations = [];
			__execTimer && clearTimeout(__execTimer);
			__execTimer = setTimeout(__nextExecution, __execDelayMS);
		};

		proto.__startAppletJS = function(applet) {
			if (J2S._version.indexOf("$Date: ") == 0)
				J2S._version = (J2S._version.substring(7) + " -").split(" -")[0]
						+ " (J2S)";
			if (!Clazz._4Name("java.lang.Class", null, null, true, false, true)) {
				if (J2S._isFile) {
					J2S.cantLoadLocalFiles();
					return;
				}
				alert("There was an unknown problem loading java.lang.Class.");
			}
			J2S._registerApplet(applet._id, applet);
			if (J2S._appArgs || applet.__Info.args == "?") {
				applet.__Info.args = (J2S._appArgs ? decodeURIComponent(J2S._appArgs).split("|") : []);
			}
			J2S._lang && (applet.__Info.language = J2S._lang);
			var isApp = applet._isApp = !!applet.__Info.main; 
			try {
				var codePath = applet._j2sPath + "/";
				if (codePath.indexOf("://") < 0) {
					var base = document.location.href.split("#")[0]
							.split("?")[0].split("/");
					if (codePath.indexOf("/") == 0)
						base = [ base[0], codePath.substring(1) ];
					else
						base[base.length - 1] = codePath;
					codePath = base.join("/");
				}
				applet._j2sFullPath = codePath.substring(0, codePath.length - 1);
				var clazz = (applet.__Info.main || applet.__Info.code);
				try {
					if (clazz.indexOf(".") < 0) {
						clazz = "_." + clazz;
						if (isApp)
							applet.__Info.main = clazz;
						else
							applet.__Info.code = clazz;
					}
					
					var cl = Clazz.loadClass(clazz);
					//cl.$static$ && cl.$static$();
					if (clazz.indexOf("_.") == 0)
						J2S.setWindowVar(clazz.substring(2), cl);
					applet.__Info.headless = (J2S._headless || isApp && (cl.$j2sHeadless || cl.j2sHeadless
							|| cl.superclazz && cl.superclazz.j2sHeadless));
					if (applet.__Info.headless) {
						Clazz._isHeadless = "true";
						System.out.println("j2sApplet running headlessly");
					}
				} catch (e) {
					alert("Java class " + clazz + " was not found.");
					return;
				}
				if (applet.__Info.code)
					codePath += applet.__Info.code.replace(/\./g, "/");
				codePath = codePath.substring(0,
						codePath.lastIndexOf("/") + 1);
				if (isApp && applet.__Info.headless) {
					applet._codePath = codePath;
					Clazz.loadClass("java.lang.Thread").currentThread$().group.html5Applet = applet;
					cl.main$SA(applet.__Info.args || []);
					System.exit$(0);
				} else {
					
					var viewerOptions = Clazz.new_("java.util.Hashtable");
					viewerOptions.put = viewerOptions.put$O$O;
					J2S._setAppletParams(applet._availableParams,
							viewerOptions, applet.__Info, true);
					viewerOptions.put("name", applet._id);// + "_object");
					viewerOptions.put("syncId", J2S._syncId);
					viewerOptions.put("fullName", applet._id + "__" + J2S._syncId + "__");
					if (J2S._isAsync)
						viewerOptions.put("async", true);
					if (applet._startupScript)
						viewerOptions.put("script", applet._startupScript)
					viewerOptions.put("platform", applet._platform);
					viewerOptions.put("documentBase", document.location.href);
					viewerOptions.put("codePath", codePath);
					viewerOptions.put("appletReadyCallback",
							"J2S.readyCallback");
					viewerOptions.put("applet", true);
					if (applet._color)
						viewerOptions.put("bgcolor", applet._color);
					if (J2S._syncedApplets.length)
						viewerOptions
								.put("synccallback", "J2S._mySyncCallback");
					viewerOptions.put("signedApplet", "true");
					if (applet._is2D && !isApp)
						viewerOptions.put("display", applet._id + "_canvas2d");
					var w = applet.__Info.width;
					var h = applet.__Info.height;
					if (w > 0 && h > 0 && (!applet._canvas || w != applet._canvas.width
							|| h != applet._canvas.height)) {
						// developer has used static { J2S.thisApplet.__Info.width=...}
						J2S.$(applet, "appletinfotablediv").width(w).height(h);
						applet._newCanvas(true);
					}
					applet._newApplet(viewerOptions);
				}
			} catch (e) {
				System.out.println((J2S._isAsync ? "normal async abort from "
						: "")
						+ e + (e.stack ? "\n" + e.stack : ""));
				return;
			}

			//applet._jsSetScreenDimensions();
			__nextExecution();
		};

		proto.__startAppletJS.j2sname = "__START_APPLET__";

		if (!proto._restoreState)
			proto._restoreState = function(clazzName, state) {
				// applet-dependent
			}

		proto._jsSetScreenDimensions = function() {
			if (!this._appletPanel)
				return
 // strangely, if CTRL+/CTRL- are used repeatedly, then the
			// applet div can be not the same size as the canvas if there
			// is a border in place.
			var d = J2S._getElement(this, (this._is2D ? "canvas2d" : "canvas"));
			this._appletPanel.setScreenDimension$I$I(d.width, d.height);
		};

		proto._show = function(tf) {
			J2S.$setVisible(J2S.$(this, "appletdiv"), tf);
			if (tf && !this._isSwing) // SwingJS applets will handle their own
										// repainting
				J2S.repaint(this, true);
		};

		proto._canScript = function(script) {
			return true
		};

		proto._processGesture = function(touches, frameViewer) {
			(frameViewer || this._appletPanel)
					.processTwoPointGesture$FAAA(touches);
		}

		proto._processEvent = function(type, xym, ev, frameViewer) {
			// xym is [x,y,modifiers,wheelScroll]
			// also processes key events
			(frameViewer || this._appletPanel).processMouseEvent$I$I$I$I$J$O$I(
					type, xym[0], xym[1], xym[2], System.currentTimeMillis$(),
					ev, xym[3]);
		}

		proto._resize = function() {
			var s = "__resizeTimeout_" + this._id;
			// only at end
			if (J2S[s])
				clearTimeout(J2S[s]);
			var me = this;
			J2S[s] = setTimeout(function() {
				J2S.repaint(me, true);
				J2S[s] = null
			}, 100);
		}

		return proto;
	};

	J2S.repaint = function(applet, asNewThread) {
		// JmolObjectInterface
		// asNewThread: true is from RepaintManager.repaintNow()
		// false is from Repaintmanager.requestRepaintAndWait()
		// called from apiPlatform Display.repaint()

		// alert("repaint " + Clazz._getStackTrace())
		if (!applet || !applet._appletPanel)
			return;

		// asNewThread = false;
		var container = J2S.$(applet, "appletdiv");
		var w = Math.round(container.width());
		var h = Math.round(container.height());
		if (applet._is2D && !applet._isApp
				&& (applet._canvas.width != w || applet._canvas.height != h)) {
			applet._newCanvas(true);
			applet._appletPanel
					.setDisplay$swingjs_api_js_HTML5Canvas(applet._canvas);
		}
		applet._appletPanel.setScreenDimension$I$I(w, h);
		var f = function() {
//			if (applet._appletPanel.top) {
//				System.out.println("j2sApplet invalidate");
//				applet._appletPanel.top.invalidate$();
//				System.out.println("j2sApplet repaint");
//				applet._appletPanel.top.repaint$();
//			}
		};
		//if (asNewThread) {
			//self.setTimeout(f,20); // requestAnimationFrame
		//} else {
			f();
		//}
	}

	/**
	 * loadImage is called for asynchronous image loading. If bytes are not
	 * null, they are from a ZIP file. They are processed sychronously here
	 * using an image data URI. Can all browsers handle MB of data in data URI?
	 * 
	 */
	J2S.loadImage = function(platform, echoName, path, bytes, fOnload, image) {
		// JmolObjectInterface
		var id = "echo_" + echoName + path + (bytes ? "_" + bytes.length : "");
		var canvas = J2S.getHiddenCanvas(platform.vwr.html5Applet, id, 0, 0,
				false, true);
		if (canvas == null) {
			if (image == null) {
				image = new Image();
				if (bytes == null) {
					image.onload = function() {
						J2S.loadImage(platform, echoName, path, null, fOnload,
								image)
					};
					image.src = path;
					return null;
				} else {
					System.out
							.println("j2sApplet J2S.loadImage using data URI for "
									+ id)
				}
				image.src = (typeof bytes == "string" ? bytes : "data:"
						+ Clazz.loadClass("javajs.util.Rdr")
								.guessMimeTypeForBytes$BA(bytes) + ";base64,"
						+ Clazz.loadClass("javajs.util.Base64").getBase64$BA(bytes));
			}
			var width = image.width;
			var height = image.height;
			if (echoName == "webgl") {
				// will be antialiased
				width /= 2;
				height /= 2;
			}
			canvas = J2S.getHiddenCanvas(platform.vwr.html5Applet, id, width,
					height, true, false);
			canvas.imageWidth = width;
			canvas.imageHeight = height;
			canvas.id = id;
			canvas.image = image;
			J2S.setCanvasImage(canvas, width, height);
			// return a null canvas and the error in path if there is a problem
		} else {
			System.out.println("j2sApplet J2S.loadImage reading cached image for " + id)
		}
		return (bytes == null ? fOnload(canvas, path) : canvas);
	};

	J2S._canvasCache = {};

	J2S.getHiddenCanvas = function(applet, id, width, height, forceNew,
			checkOnly) {
		id = applet._id + "_" + id;
		var d = J2S._canvasCache[id];
		if (checkOnly)
			return d;
		if (forceNew || !d || d.width != width || d.height != height) {
			d = document.createElement('canvas');
			// for some reason both these need to be set, or maybe just d.width?
			d.width = d.style.width = width;
			d.height = d.style.height = height;
			d.id = id;
			J2S._canvasCache[id] = d;
		}

		return d;
	}

	J2S.setCanvasImage = function(canvas, width, height) {
		// called from org.J2S.awtjs2d.Platform
		canvas.buf32 = null;
		canvas.width = width;
		canvas.height = height;
		canvas.getContext("2d").drawImage(canvas.image, 0, 0,
				canvas.image.width, canvas.image.height, 0, 0, width, height);
	};

	J2S.applyFunc = function(f, a) {
		// J2SObjectInterface
		return f(a);
	}

	J2S.setDraggable = function(tag, targetOrArray) {

		// draggable tag object; target is itself

		// J2S.setDraggable(tag)
		// J2S.setDraggable(tag, true)

		// draggable tag object that controls another target,
		// either given as a DOM element or jQuery selector or function
		// returning such

		// J2S.setDraggable(tag, target)
		// J2S.setDraggable(tag, fTarget)

		// draggable tag object simply loade=s/reports mouse position as
		// fDown({x:x,y:y,dx:dx,dy:dy,ev:ev}) should fill x and y with starting
		// points
		// fDrag(xy) and fUp(xy) will get {x:x,y:y,dx:dx,dy:dy,ev:ev} to use as
		// desired

		// J2S.setDraggable(tag, [fAll])
		// J2S.setDraggable(tag, [fDown, fDrag, fUp])

		// unbind tag

		// J2S.setDraggable(tag, false)

		// draggable frames by their titles.
		// activation of dragging with a mouse down action
		// deactivates all other mouse operation in SwingJS
		// until the mouse is released.
		// uses jQuery outside events - v1.1 - 3/16/2010 (see j2sJQueryExt.js)

		// J2S.setDraggable(titlebar, fGetFrameParent), for example, is issued
		// in swingjs.plaf.JSFrameUI.js

		var drag, up;

		var dragBind = function(isBind) {

			$tag.unbind('mousemoveoutjsmol');
			$tag.unbind('touchmoveoutjsmol');
			$tag.unbind('mouseupoutjsmol');
			$tag.unbind('touchendoutjsmol');
			$tag.unbind('pointeroutjsmol');
			J2S._dmouseOwner = null;
			tag.isDragging = false;
			tag._isDragger = false;
			if (isBind) {
				$tag.bind('mousemoveoutjsmol pointeroutjsmol touchmoveoutjsmol', function(ev) {
					drag && drag(ev);
				});
				$tag.bind('mouseupoutjsmol pointeroutjsmol touchendoutjsmol', function(ev) {
					up && up(ev);
				});
			}
		};

		var $tag = $(tag);
		tag = $tag[0];
		if (!tag || tag._isDragger)
			return;

		var target, fDown, fDrag, fUp;
		if (targetOrArray === false) {
			dragBind(tag, false);
			return;
		}
		if (targetOrArray instanceof Array) {
			// J2S.setDraggable(tag, [fAll])
			// J2S.setDraggable(tag, [fDown, fDrag, fUp])
			fDown = targetOrArray[0];
			fDrag = targetOrArray[1] || fDown;
			fUp = targetOrArray[2] || fDown;
		} else {
			// J2S.setDraggable(tag)
			// J2S.setDraggable(tag, true)
			// J2S.setDraggable(tag, target)
			// J2S.setDraggable(tag, fTarget)
			target = (targetOrArray !== true && targetOrArray || tag);
			// allow for a function to return the target
			// this allows the target to be created after the call to
			// J2S.setDraggable()
			if (!(typeof target == "function")) {
				var t = target;
				target = function() {
					return $(t).parent()
				}
			}
		}

		tag._isDragger = true;

		var x, y, dx, dy, pageX0, pageY0, pageX, pageY;

		var down = function(ev) {
			J2S._dmouseOwner = tag;
			J2S._dmouseDrag = drag;
			J2S._dmouseUp = up;

			tag.isDragging = true; // used by J2S mouse event business
			pageX = Math.round(ev.pageX);
			pageY = Math.round(ev.pageY);
			var xy = {
				x : 0,
				y : 0,
				dx : 0,
				dy : 0,
				ev : ev
			};
			if (fDown) {
				fDown(xy, 501);
			} else if (target) {
				var o = $(target(501)).position();
				if (!o) return false;
				xy = {
					x : Math.round(o.left),
					y : Math.round(o.top)
				};
			}
			pageX0 = xy.x;
			pageY0 = xy.y;
			return false;
		}, drag = function(ev) {
			// we will move the frame's parent node and take the frame along
			// with it
			var ev0 = ev.ev0 || ev;
			if (ev0.buttons == 0 && ev0.button == 0)
				tag.isDragging = false;
			var mode = (tag.isDragging ? 506 : 503);
			if (!J2S._dmouseOwner || tag.isDragging && J2S._dmouseOwner == tag) {
				x = pageX0 + (dx = Math.round(ev.pageX) - pageX);
				y = pageY0 + (dy = Math.round(ev.pageY) - pageY);
				if (isNaN(x))return;
				if (fDrag) {
					fDrag({
						x : x,
						y : y,
						dx : dx,
						dy : dy,
						ev : ev
					}, mode);
				} else if (target) {
					var frame = target(mode, x, y);
					if (frame)
						$(frame).css({ top : y + 'px', left : x + 'px'})
				}
			}
		}, up = function(ev) {
			J2S._dmouseDrag = null;
			J2S._dmouseUp = null;
			if (J2S._dmouseOwner == tag) {
				tag.isDragging = false;
				J2S._dmouseOwner = null
				if (isNaN(x))return;
				fUp && fUp({
					x : x,
					y : y,
					dx : dx,
					dy : dy,
					ev : ev
				}, 502);
				return false;
			} else {
// if (ev.ev0)
//				setTimeout(function(){document.body.dispatchEvent(ev.ev0.originalEvent)},50)
			}
		};

		var fixTouch = function(ev) {
			if (ev.originalEvent.targetTouches) {
				ev.pageX = Math.round(ev.originalEvent.targetTouches[0].pageX);
				ev.pageY = Math.round(ev.originalEvent.targetTouches[0].pageY);
			}
			return ev;
		}

		$tag.bind('pointerdown mousedown touchstart', function(ev) {
			return down && down(fixTouch(ev));
		});

		$tag.bind('pointermove mousemove touchmove', function(ev) {
			return drag && drag(fixTouch(ev));
		});

		$tag.bind('pointerup mouseup touchend', function(ev) {
			// touchend does not express a position, and we don't use it anyway
			return up && up(ev);
		});

		dragBind(true);

	}

	J2S.setWindowZIndex = function(node, z) {
		// on frame show or mouse-down, create a stack of frames and sort by
		// z-order
		if (!node || !node.ui || node.ui.embeddedNode)
			return 
		var app = node.ui.jc.appContext.threadGroup.name + "_";
		var a = [];
		var zmin = 1e10
		var zmax = -1e10
		var $windows = $("body > div > .swingjs-window").not("body > .swingjs-tooltip :first-child");
		var found = false;
		$windows.each(function(c, b) {
			  if (b == node)
				  found = true;
			  if (b.id.indexOf(app) == 0)
			    	a.push([ (b == node ? z : +b.style.zIndex), b ]);
		});
		a.sort(function(a, b) {
			return a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0
		})
		var zbase = z = J2S._z.rear + 2000;
		var modalZ = 1e10;
		for (var i = 0, i1 = a.length; i < i1; i++) {
			var n = a[i][1];
			if (n == node)
				z = zbase;
			if (!n.ui || !n.ui.embeddingNode) {
			  n.style.zIndex = zbase;
			  if (n.ui && n.ui.outerNode && !n.ui.embeddingNode) {
				  n.ui.outerNode.style.zIndex = zbase;
				  if (n.ui.jc.isVisible$() && n.ui.modalNode) {
					  if (zbase < modalZ)
						  modalZ = zbase;
					  n.ui.modalNode.style.zIndex = zbase - 1;
				  } 
			  }
			}
			zbase += 1000;
		}
		if (!found)
			z += 1000;
		node.style.position = "absolute";
		if (!node.ui.jc.modal && modalZ < 1e10 && z > modalZ) {
			z = modalZ - 500;
		}
		node.ui.outerNode && (node.ui.outerNode.style.zIndex = z);
		return z;
	}

	J2S.say = function(msg) {
		alert(msg);
	}

	J2S.Swing = {
		// a static class for menus and other resources
		count : 0,
		menuInitialized : 0,
		menuCounter : 0
	};

	J2S.getSwing = function() {
		return J2S.Swing
	}

	J2S.showInfo = function(applet, tf) {
		applet._showInfo(tf);
	}

	J2S.Loaded = {};

	J2S.isResourceLoaded = function(resource, done) {
		path = J2S.getResourcePath(resource, true);
		var r = J2S.Loaded[resource];
		if (done)
			J2S.Loaded[resource] = 1;
		return r;
	}

	J2S.getResourcePath = function(path, isJavaPath) {
		if (!path || path.indexOf("https:/") != 0
				&& path.indexOf("https:/") != 0 && path.indexOf("file:/") != 0) {
			Clazz.loadClass("swingjs.JSUtil");
			var applet = swingjs.JSUtil.getApplet$();
			path = (!isJavaPath && applet.__Info.resourcePath || applet.__Info.j2sPath)
					+ "/" + (path || "");
		}
		return path;
	}

	J2S._newGrayScaleImage = function(context, image, width, height, grayBuffer) {
		var c;
	  image || (image = Jmol.$(context.canvas.applet, "image")[0]);
		if (image == null) {
			var appId = context.canvas.applet._id;
	    var id = appId + "_imagediv";
			c = document.createElement("canvas");
			c.id = id;
			c.style.width = width + "px";
			c.style.height = height + "px";
			c.width = width;
			c.height = height;

			var layer = document.getElementById(appId + "_contentLayer");
			image = new Image();
			image.canvas = c;
			image.appId = appId;
			image.id = appId + "_image";
			image.layer = layer;
			image.w = width;
			image.h = height;
			image.onload = function(e) {
				try {
				  URL.revokeObjectURL(image.src);
				} catch (e) {}
			};
			var div = document.createElement("div");
			image.div = div;
			div.style.position="absolute";
			layer.appendChild(div);
			div.appendChild(image);
		}
		c = image.canvas.getContext("2d");
		var imageData = c.getImageData(0, 0, width, height);
		var buf = imageData.data;
		var ng = grayBuffer.length;
		var pt = 0;
		for (var i = 0; i < ng; i++) {
			buf[pt++] = buf[pt++] = buf[pt++] = grayBuffer[i];
			buf[pt++] = 0xFF;
		}
		c.putImageData(imageData, 0, 0);
		image.canvas.toBlob(function(blob){image.src = URL.createObjectURL(blob)});
		return image;
	}

	J2S.getCaller = function() { return arguments.callee.caller.caller}

})(self.J2S, self.jQuery, window, document);
// j2sClazz.js 
// NOTE: updates to this file should be copied to j2sjmol.js

// latest author: Bob Hanson, St. Olaf College, hansonr@stolaf.edu

// NOTES by Bob Hanson

// Google closure compiler cannot handle Clazz.new or Clazz.super

// BH 2024.06.22 adds Integer.getIngeger(String, int) (returning null)
// BH 2024.03.03 removes unnecessary loadClass("xxxx") on exceptionOf(e,"xxxx") call
// BH 2024.02.23 fixes missing Long.signum
// BH 2023.04.30 fixes issues when Info.console == window.console
// BH 2023.03.01 upgrade for Java11 String, including String.isBlank() and CharSequence.lines(String) (in Java11 this is StringRoman1.lines(byte[])
// BH 2023.02.12 upgrade for (asynchronous?) packaging
// BH 2023.01.22 fix for Double.doubleToRawLongBits missing and Float.floatToIntBits failing on NaN
// BH 2023.01.15 fix for int[2][3][] not initializing properly
// BH 2022.12.03 fix for Double.isInfinite should not be true for NaN
// BH 2022.12.03 fix for Double.parseDouble("") and new Double(NaN) should be NaN, not 0
// BH 2022.09.20 fix for Class.forName not loading static inner classes directly
// BH 2022.09.20 fix for default toString for classes using "." name not "$" name for inner classes
// BH 2022.09.15 fix for new Error() failing; just letting java.lang.Error subclass Throwable
// BH 2022.09.08 Fix new Test_Inner().getClass().getMethod("testDollar", new Class<?>[] {Test_Abstract_a.class}).getName()
// BH 2022.04.19 TypeError and ResourceError gain printStackTrace$() methods
// BH 2022.03.19 String.valueOf(Double) does not add ".0"
// BH 2022.01.17 fixes interface default method referencing own static fields
// BH 2021.12.19 adds Double -0; fixes println(Double)
// BH 2021.12.15 default encoding for String.getBytes() should be utf-8.
// BH 2021.08.16 fix for Interface initializing its subclass with static initialization
// BH 2021.07.28 String.instantialize upgraded to use TextDecoder() if possible (not in MSIE)
// BH 2021.07.20 Date.toString() format yyyy moved to end, as in Java 
// BH 2021.06.11 Number.compareTo(....) missing
// BH 2021.02.12 implements better(?) interface defaults resolution -- in order of presentation

// see earlier notes at net.sf.j2s.java.core.srcjs/js/devnotes.txt

//window["j2s.object.native"] = true;  // this is not an option

/******************************************************************************
 * Copyright (c) 2007 java2script.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zhou Renjian - initial API and implementation
 *****************************************************************************/
/*******************************************************************************
 * @author zhou renjian
 * @create Nov 5, 2005
 ******************************************************************************/
 

// encapsulating function

;(function(J2S, window, document) {

  if (J2S.clazzLoaded) return;
  J2S.clazzLoaded = true;
		
  // at least for now:

  var setWindowValue = function(a, v) { window[a] = v; }

J2S.LoadClazz = function(Clazz) {
	
Clazz.setTVer = function(ver) { // from class loading
	if (Clazz._VERSION_T.split('-')[0] != ver.split('-')[0])
		System.err.println("transpiler was " + Clazz._VERSION_T + " now " + ver + " for " + lastLoaded);
	Clazz._VERSION_T = ver;
}

var lastLoaded;
var consoleDiv = J2S.getGlobal("j2s.lib").console;

Clazz.setConsoleDiv = function(d) {
	J2S.getGlobal("j2s.lib").console = consoleDiv = d;
  };

Clazz.ClassFilesLoaded = [];

Clazz.popup = Clazz.log = Clazz.error = window.alert;

/* can be set by page JavaScript */
Clazz.defaultAssertionStatus = false;

/* can be set by page JavaScript */
Clazz._assertFunction = null;


// ////// 16 methods called from code created by the transpiler ////////

var getArrayClass = function(name){
	// "[C" "[[C"
	var n = 0;
	while (name.charAt(n) == "[") n++;
	var type = name.substring(n);
	if (type == "S")
		type = "H"; // [S is short[] in Java
	var clazz = (type.length == 1 ? primTypes[type].TYPE : Clazz._4Name(type.split(";")[0].substring(1),null,null,true)); 
	return Clazz.array(clazz,-n);
}

Clazz.array = function(baseClass, paramType, ndims, params, isClone) {
	
	if (arguments.length == 2 && paramType < 0)
		return arrayClass(baseClass, -paramType);

	var t0 = (_profileNew ? window.performance.now() : 0);

	var ret = _array.apply(null, arguments);
	
	_profileNew && addProfileNew(baseClass == -1 ? paramType.__BASECLASS : baseClass, -1);

	return ret;
}

var _array = function(baseClass, paramType, ndims, params, isClone) {
	
  // Object x = Array.newInstance(componentClass, nElements);
  // var x=Clazz.array((Clazz.array(componentClass, 3);

	
  // int[][].class Clazz.array(Integer.TYPE, -2)
  // new int[] {3, 4, 5} Clazz.array(Integer.TYPE, -1, [3, 4, 5])
  // new int[][]{new int[] {3, 4, 5}, {new int[] {3, 4, 5}}
  // Clazz.array(Integer.TYPE, -2, Clazz.array(Integer.TYPE, -1, [3, 4, 5]),
	// Clazz.array(Integer.TYPE, -1, [3, 4, 5]) )
  // new int[3] Clazz.array(Integer.TYPE, [3])
  // new int[3][3] Clazz.array(Integer.TYPE, [3, 3])
  // new int[3][] Clazz.array(Integer.TYPE, [3, null])
  // new char[3] Clazz.array(Character.TYPE, [3])
  // new String[3] Clazz.array(java.lang.String, [3])

  if (arguments[0] === -1) {
    // four-parameter option from JU.AU.arrayCopyObject;
    // truncate array using slice
    // Clazz.array(-1, array, ifirst, ilast+1)
    var a = arguments[1];
    var b = a.slice(arguments[2], arguments[3]);
    return copyArrayProps(a, b);
  }
  if (arguments.length == 2 && baseClass.BYTES_PER_ELEMENT) {
	// Clazz.array(rawInt8Array, int[] array)
	// direct transfer of __* metadata (see java.nio.ByteBuffer)
	return copyArrayProps(paramType, baseClass); 
  }
  var prim = Clazz._getParamCode(baseClass);
  var dofill = true;
  if (arguments.length < 4) {
    // one-parameter option just for convenience, same as array(String, 0)
    // two-parameter options for standard new foo[n],
    // Array.newInstance(class, length), and
    // Array.newInstance(class, [dim1, dim2, dim3....])
    // three-parameter option for (Integer.TYPE, -1, [3, 4, 5])
	var cl = arguments[0];
    var baseClass = cl.__BASECLASS || cl;
    var haveDims = (typeof arguments[1] == "number");  
    var vals = arguments[haveDims ? 2 : 1];
    var ndims = (arguments.length == 1 ? 0 : !haveDims ? vals.length : arguments[1] || 0);
    if (ndims < 0 && arguments.length == 2) {
      return arrayClass(baseClass, -ndims);
    }
    if (ndims == 0) {
      ndims = -1;
      vals = [];
    }
    if (haveDims && ndims >= -1) {
      if (ndims == -1) {
        // new int[] {3, 4, 5};
        return _array(baseClass, prim + "A", -1, vals);
      }
      // Array.newInstance(int[][].class, 3);
      

      var nElem = ndims;
      cl = baseClass;

      ndims = 0;
      while ((cl = cl.getComponentType$()) != null) {
    	  baseClass = cl;
    	  ndims++;
      }
      if (ndims > 0) {
    	  a = new Array(nElem);
          setArray(a, baseClass, prim + "A", ndims + 1);
    	  for (var i = nElem; --i >= 0;)
    		  a[i] = null;
      } else {
    	  a = _array(baseClass, prim + "A", ndims + 1, [nElem]);
      }
	  return a;
    }      
    params = vals;
    paramType = prim;
    
    for (var i = Math.abs(ndims); --i >= 0;) {
      paramType += "A";
      if (!haveDims && params[i] === null) {
        params.length--;
        dofill = false;
      }
    }
    if (haveDims) {
      // new int[][] { {0, 1, 2}, {3, 4, 5} , {3, 4, 5} , {3, 4, 5} };
      return  setArray(vals, baseClass, paramType, -ndims);
    }
  }
  if (ndims < 0) {
    params = [-1, params];
  } else {
    var initValue = null;
    if (ndims >= 1 && dofill) {
    	initValue = _initVal(prim);
    }
    var p = params; // an Int32Array
    var n = p.length;
    params = new Array(n + 1);
    for (var i = 0; i < n; i++)
    	params[i] = p[i];
    params[n] = initValue;
  }
  params.push(paramType);
  var nbits = 0;
  if (ndims != 0 && !(isClone && Array.isArray(params[1]))) {
    switch (prim) {
    case "B":
      nbits = 8;
      break; 
    case "H":
      nbits = 16;
      break;
    case "I":
      nbits = 32;
      break;
    case "F":
    case "D":
    case "J":
      nbits = 64;
      break;
    }  
  }
  return newTypedA(baseClass, params, nbits, (dofill ? ndims : -ndims), isClone);
}

var _initVal = function(p) {
    switch (p) {
    case "J":
    case "B":
    case "H": // short
    case "I":
    case "F":
    case "D":
      return 0;
    case "C": 
      return  '\0';
    case "Z":
        return  false;
    default:
    	return null;
    }
}

Clazz.assert = function(clazz, obj, tf, msg) {
  if (!clazz.$_ASSERT_ENABLED_)return;
  var ok = true;
  try {
    ok = tf.apply(obj)
    if (!ok)
      msg && (msg = msg.apply(obj));  
  } catch (e) {
    ok = false;
  }
  if (!ok) {
    doDebugger();
    if (Clazz._assertFunction) {
      return Clazz._assertFunction(clazz, obj, msg || Clazz._getStackTrace());
    }
    Clazz.load("AssertionError");
    if (msg == null)
      throw Clazz.new_(AssertionError.c$);
    else
      throw Clazz.new_(AssertionError.c$$S, [msg]);
  }
}

Clazz.clone = function(me) { 
  // BH allows @j2sNative access without super constructor
if (me.__ARRAYTYPE) {
  return appendMap(Clazz.array(me.__BASECLASS, me.__ARRAYTYPE, -1, me, true), me);
}
  me = appendMap(new me.constructor(inheritArgs), me); 
  me.__JSID__ = ++_jsid;
  return me;
}

Clazz.forName = function(name, initialize, loader, isQuiet) {
  // we need to consider loading a class from the path of the calling class.
 var cl = null;
 (typeof initialize == "undefined") && (initialize = true);
 if (loader) {
	try {
		isQuiet = true;
		var className = loader.baseClass.getName$(); // set in
														// java.lang.Class.getClassLoader$()
		var i = className.lastIndexOf(".");
		var name1 = className.substring(0, i + 1);
		name1 = (name.indexOf(name1) == 0 ? name : name1 + name);
		cl = Clazz._4Name(name1, null, null, false, initialize, true);
	} catch (e) {}
 }
 return cl || Clazz._4Name(name, null, null, false, initialize, isQuiet);
}

Clazz._setDeclared = function(name, func) {
  (name.indexOf(".") < 0) && (name = "java.lang." + name);
   Clazz.allClasses[name] = func;
}

Clazz._getDeclared = function(name) { 
	(name.indexOf(".") < 0) && (name = "java.lang." + name);
	return Clazz.allClasses[name] 
}

Clazz._isClassDefined = function(clazzName) {
	if (!clazzName) 
	return false;    /* consider null or empty name as non-defined class */
	if (Clazz.allClasses[clazzName])
	return true;
	var pkgFrags = clazzName.split (/\./);
	var pkg = null;
	for (var i = 0; i < pkgFrags.length; i++)
	if (!(pkg = (pkg ? pkg[pkgFrags[i]] : Clazz._allPackage[pkgFrags[0]]))) {
	return false;
	}
	return (pkg && (Clazz.allClasses[clazzName] = pkg));
};


Clazz.getClass = function(cl, methodList) {
  // $Class$ is the java.lang.Class object wrapper
  // $clazz$ is the unwrapped JavaScript object
  cl = getClazz(cl) || cl;
  if (cl.$Class$)
    return cl.$Class$;
  java.lang.Class || Clazz.load("java.lang.Class");
  var Class_ = cl.$Class$ = new java.lang.Class();
  Class_.$clazz$ = cl; // for arrays - a bit of a hack
  Class_.$methodList$ = methodList;
  return Class_;
}


/**
 * Implements Java's keyword "instanceof" in JavaScript's way. Also alows for
 * obj to be a class itself
 * 
 * @param obj
 *            the object to be tested
 * @param clazz
 *            the class to be checked
 * @return whether the object is an instance of the class
 */
/* public */
Clazz.instanceOf = function (obj, clazz) {
	if (obj == null)
		return false;
  // allows obj to be a class already, from arrayX.getClass().isInstance(y)
  // unwrap java.lang.Class to JavaScript clazz using $clazz$
  if (typeof clazz == "string") {
    clazz = Clazz._getDeclared(clazz);
  } 
  if (!clazz)
    return false;
  if (obj == clazz)
	return true;
    // check for object being a java.lang.Class and the other not
  if (obj.$clazz$ && !clazz.$clazz$) return (clazz == java.lang.Class);
  obj.$clazz$ && (obj = obj.$clazz$);
 if (clazz == String)
	return typeof obj == "string";
  clazz.$clazz$ && (clazz = clazz.$clazz$);
  if (obj == clazz)
    return true;
  if (obj.__ARRAYTYPE || clazz.__ARRAYTYPE) {
	if (obj.__ARRAYTYPE == clazz.__ARRAYTYPE)
		return true;
	if (clazz.__BASECLASS == Clazz._O) {
		return (!obj.__ARRAYTYPE ? Array.isArray(obj) && clazz.__NDIM == 1
		: obj.__NDIM >= clazz.__NDIM && !obj.__BASECLASS.__PRIMITIVE);
	}
      return obj.__ARRAYTYPE && clazz.__ARRAYTYPE && obj.__NDIM == clazz.__NDIM 
               && isInstanceOf(obj.__BASECLASS, clazz.__BASECLASS); 
  }
  return (obj instanceof clazz || isInstanceOf(getClassName(obj, true), clazz, true));
};

/**
 * sgurin Implements Java's keyword "instanceof" in JavaScript's way **for
 * exception objects**.
 * 
 * calls Clazz.instanceOf if e is a Java exception. If not, try to detect known
 * native exceptions, like native NullPointerExceptions and wrap it into a Java
 * exception and call Clazz.instanceOf again. if the native exception can't be
 * wrapped, false is returned.
 * 
 * @param obj
 *            the object to be tested
 * @param clazz
 *            the class to be checked
 * @return whether the object is an instance of the class
 * @author: sgurin
 */
Clazz.exceptionOf = function(e, clazz) {
  if(e.__CLASS_NAME__) {
	  if (typeof clazz == "string") {
		  var c = Clazz._getDeclared(clazz);
		  if (!c) return false;
		  clazz = c;
	  }
    return Clazz.instanceOf(e, clazz);
  }
  if (!e.getMessage) {
    e.getMessage = function() {return "" + e};
  }
  if (!e.printStackTrace$) {
    e.printStackTrace$ = function(){System.err.println$S(e + "\n" + this.stack)};
    e.printStackTrace$java_io_PrintStream = function(stream){
    	stream.println$S(e + "\n" + e.stack);
    };
    // alert(e + " try/catch path:" + Clazz._getStackTrace(-10));
  }
  if(clazz == Error) {
    if (("" + e).indexOf("Error") < 0)
      return false;
    System.err.println$O(Clazz._getStackTrace());
    return true;
    // everything here is a Java Exception, not a Java Error
  }
  return (clazz == Exception || clazz == Throwable
    || clazz == NullPointerException && _isNPEExceptionPredicate(e));
};

var initStatic = function(cl, impls) {
	if (impls) {
		for (var i = 0; i < impls.length; i++) {
			initStatic(impls[i], impls[i].implementz);
		}
	} else if (cl.superclazz) {
			initStatic(cl.superclazz);
	}
	cl.$static$ && (initStatics(cl), cl.$static$());
}

/**
 * Load a class by name or an array representing a nested list of inner classes.
 * Just finalize this class if from $clinit$.
 */
Clazz.load = function(cName, from$clinit$) {
  if (!cName)
    return null;
  var cl = cName;  
  switch (from$clinit$ || 0) {
  case 1:
    // C$.$clinit$ call to finalize all dependencies
	cl.$clinit$ = 0-cl.$clinit$;
	// -2 means v 3.2.6
	// -1 means v 3.2.5
	// NaN means original 3.2.4 function() {Clazz.load(C$, 1)};
    var ld = cl.$load$;
    setSuperclass(cl, (ld && ld[0] ? Clazz.load(ld[0]) : null));
    ld[1] && addInterface(cl, ld[1]);
    switch (cl.$clinit$) {
    case -1:
    	// done
    	break;
    case -2:
    	initClass0(cl);
    	break;    
    }
    return;
  case 2:
	// C$.$static$ to do static initialization
 	if (cl.$load$) {
        if (cl.$load$[0] && !cl.superclazz) {
        	// can happen with Clazz.new_($I(n,1)....)
          setSuperclass(cl, Clazz.load(cl.$load$[0]));
        }
 		cl.$load$ = 0;
	initStatic(cl, cl.$isInterface ? cl.implementz : 0);
  	}
	return;
  }
  // allow for nested calling: ["foo",".foo_inner1",".foo_inner2"]
  if (cName instanceof Array) {
    var cl1 = null;
    var name;
    for (var i = 0; i < cName.length; i++) {
      var cn = cName[i];
      cl1 = Clazz.load(name = (cn.indexOf(".") == 0 ? name + cn : cn));
    }
    return cl1;
  }
  // allow for a clazz itself
  if (cl.__CLASS_NAME__)
    return Clazz._initClass(cl,1,1,0);
  // standard load of class by name
  if (cName.indexOf("Thread.") == 0)
    Clazz._4Name("java.lang.Thread", null, null, true)
  if (cName.indexOf("Thread") == 0)
    cName = "java.lang." + cName;
  return Clazz._4Name(cName, null, null, true);
}

// create and $init0$
var initClass0 = function(c) {
	var fields = c.$fields$;
	var objects = fields && fields[0];
	createDefaults(c, objects, false);
	fields && initStatics(c);
}

var initStatics = function(c) {
	var statics = c.$fields$ && c.$fields$[1];
	if (statics && statics.length)
	createDefaults(c, statics, true);
}

// C$.$fields$=[
// ['I',['test3','itype'],'S',['test1'],'O',['test2','java.util.List[]','test4','test.Test_','+test5']],
// ['D',['d'],'F',['f'],'I',['itest1','itest2'],'S',['stest1']]
// ]
var createDefaults = function(c, data, isStatic) {
	var a = getFields(c, data, true);
	if (isStatic) {
		for (var i = a.length; --i >= 0;) {
			var j = a[i][0];
			if (c[j] != undefined)
				return;
			c[j] = a[i][1];
		}
		return;
	}
	c.$init0$ = function(){
			var cs = c.superclazz;
			cs && cs.$init0$ && cs.$init0$.apply(this);
			for (var i = a.length; --i >= 0;){
				this[a[i][0]] = a[i][1];
			}
		};
		
}

Clazz._getFieldNames = function(c, isStatic) {
	return (c.$fields$ ? getFields(c, c.$fields$[isStatic ? 1 : 0], 0) : []);
}

Clazz._getFieldTypes = function(c, isStatic) {
	return (c.$fields$ ? getFields(c, c.$fields$[isStatic ? 1 : 0], "types") : []);
}

var fieldTypes = "Integer;Float;Double;Character;Long;Byte;"
/**
 * Get correct default (0, '\0', null) or just return a list of names.
 */
var getFields = function(c, data, andDefaults) {
  	var a = [];
  	if (!data)
  		return a;
  	if (andDefaults == "types") {
		for (var i = 0, n = data.length; i < n; i++) {
			var type = data[i++];
			var anames = data[i];
			if (type != "O") {
				for (var j = anames.length; --j >= 0;)
				a.push(type);
				continue;
			}
			type = "String";
  			for (var j = 0, na = anames.length; j < na; j++) {
  				if (anames[j].indexOf("+") != 0) {
  					type = anames[++j];
  				}
  				a.push(type);
  			}
  			continue;
		}			
		return a;
  	} 
	if (andDefaults) {
		for (var i = 0, n = data.length; i < n; i++) {
			var type = data[i++];
			var anames = data[i];
			var defval;
			switch (type) {
			case 'S':
				defval = null;
				break;
			case 'O':
				for (var j = 0, na = anames.length; j < na; j++) {
					var name = anames[j];
					if (name.indexOf("+") == 0)
						name = name.substring(1);
					else
						j++;
					a.push([name, null]);
				}
				continue;
			case 'C':
				defval = '\0';
				break;
			case 'Z':
				defval = false;
				break;
			default:
				defval = 0;
				break;  		
			}
			for (var j = 0, na = anames.length; j < na; j++) {	
				a.push([anames[j], defval]);
			}
		}	
  		return a;
  	}
  	for (var i = 0, n = data.length; i < n; i++) {
  		var type = data[i++];
  		var anames = data[i];
  		if (type == 'O') {
			for (var j = 0, na = anames.length; j < na; j++) {
  				var name = anames[j];
  				if (name.indexOf("+") == 0)
  					name = name.substring(1);
  				else
  					j++;
  				a.push(name);
  			}				
  		} else {
			for (var j = 0, na = anames.length; j < na; j++) {	
				a.push(anames[j]);
			}
  		}
  	}	
  	return a;
}
Clazz._newCount = 0;

/**
 * Create a new instance of a class. Accepts: a string
 * Clazz.new_("java.util.Hashtable") a clazz (has .__CLASS_NAME__ and a default
 * contructor) a specific class constructor such as c$$S a constructor from a
 * one class (c, anonymous constructor) and a class to create, cl
 * 
 */
  
Clazz.new_ = function(c, args, cl) {
  if (!c)
    return new Clazz._O();

  var a = arguments;
  if (Array.isArray(c)) {
		a = [args, c];
		if (arguments.length == 3)
			a.push(cl);
		var _ = args;args = c;c = _;
	}
  var generics;
  if (c === 1) { // new for 3.2.6 {K:"java.lang.String",...}
	generics = arguments[1];
	a = [];
	c = a[0] = arguments[2];
	args = a[1] = arguments[3];
	cl = a[2] = arguments[4];
	a = a.slice(0, arguments.length - 2);
  }
  var haveArgs = !!args;
  args || (args = [[]]);
  
  Clazz._newCount++;
  
  var t0 = (_profileNew ? window.performance.now() : 0);
  
  if (c.__CLASS_NAME__ && c.c$) 
    c = c.c$;
  else if (typeof c == "string") {
	// Clazz.new_("path.className")
	// Clazz.new_("path.className","$I$O...",[3,"test"]);
	switch(arguments.length) {
	case 1:
		return Clazz.new_(Clazz.load(c));
	case 3:
		return Clazz.new_(Clazz.load(c)["c$" + args], cl)
	}
  }
    
  // an inner class will attach arguments to the arguments returned
  // Integer will be passed as is here, without c.exClazz, or cl
  var clInner = cl;
  cl = cl || c.exClazz || c;
  Clazz._initClass(cl,1,0,0); 
  // BH note: Critical here that the above is not 1,1,0;
  // static init is the responsibility of newInstance
  // or a static field or method call (which is handled
  // by the $I$(n) handler in the function initializer in
  // the newClass() call.
  var obj = new (Function.prototype.bind.apply(cl, a));
  if (args[2] != inheritArgs) {
    haveArgs && c.apply(obj, args);
    clInner && clInner.$init$.apply(obj);
  }
    
  _profileNew && addProfileNew(cl, window.performance.now() - t0);

  if (generics) {
	obj.$init$.generics = generics;
  }
  return obj;
}

// var C$=Clazz.newClass(P$,
// "Test_Local$1",
// function(){Clazz.newInstance(this, arguments[0],1,C$);},
// Clazz.load('test.Test_Local$1ReducingSink'), null, 1);
//

Clazz.newClass = function (prefix, name, clazz, clazzSuper, interfacez, type) { 
// if (J2S._debugCore) {
// var qualifiedName = (prefix ? (prefix.__PKG_NAME__ || prefix.__CLASS_NAME__)
// + "." : "") + name;
// checkDeclared(qualifiedName, type);
// }
  clazz || (clazz = function () {Clazz.newInstance(this,arguments,0,clazz)});  
  
  clazz.__NAME__ = name;
  // prefix class means this is an inner class, and $this$0 refers to the
	// outer class.
  // no prefix class but a super class that is an inner class, then $this$0
	// refers to its $this$0.
  // there can be a conflict here.
  prefix.__CLASS_NAME__ && (clazz.$this$0 = prefix.__CLASS_NAME__) || clazzSuper && clazzSuper.$this$0 && (clazz.$this$0 = clazzSuper.$this$0);

  
  clazz.$load$ = [clazzSuper, interfacez];
  clazz.$isEnum = clazzSuper == 'Enum';
  // get qualifed name, and for inner classes, the name to use to refer to
	// this
  // class in the synthetic reference array b$[].

  var qName, bName;
  if (!prefix) {
    // e.g. Clazz.declareInterface (null, "ICorePlugin",
	// org.eclipse.ui.IPlugin);
    qName = name;
    Clazz._setDeclared(name, clazz);
  } else if (prefix.__PKG_NAME__) {
    // e.g. Clazz.declareInterface (org.eclipse.ui, "ICorePlugin",
	// org.eclipse.ui.IPlugin);
    qName = prefix.__PKG_NAME__ + "." + name;
    prefix[name] = clazz;
    if (prefix === java.lang) {
      setWindowValue(name, clazz);
    }
  } else {
    // is an inner class
    qName = prefix.__CLASS_NAME__ + "." + name;
    bName = prefix.__CLASS_NAME__ + "$" + name;    
    prefix[name] = clazz;
  }
  
  finalizeClazz(clazz, qName, bName, type, false);

// for (var i = minimalObjNames.length; --i >= 0;) {
// var name = minimalObjNames[i];
// clazz[name] = objMethods[name];
// }
  Clazz._setDeclared(qName, clazz);
  return clazz;

};

Clazz.newEnumConst = function(vals, c, enumName, enumOrdinal, args, cl) {
	var clazzEnum = c.exClazz;
	var e = clazzEnum.$init$$ || (clazzEnum.$init$$ = clazzEnum.$init$);
	clazzEnum.$init$ = function() {e.apply(this); this.name = this.$name = enumName; this.ordinal = enumOrdinal;this.$isEnumConst = true;}
	vals.push(clazzEnum[enumName] = clazzEnum.prototype[enumName] = Clazz.new_(c, args, cl));
	}
	
Clazz.newInstance = function (objThis, args, isInner, clazz) {
  if (args && ( 
     args[0] == inheritArgs 
     || args[1] == inheritArgs 
     || args[2] == inheritArgs 
  )) {
    // Just declaring a class, not creating an instance or doing field
	// preparation.
    // That is, we are just generating the prototypes for this method using new
	// superClass()
    return;
  }

  if (objThis.__VAL0__) {
    // Integer, Long, Byte, Float, Double
    // .instantialize(val)
    objThis.valueOf = function () {
      return this;
    };
  }

  objThis.__JSID__ = ++_jsid;

  if (!isInner) {
// if (args)
	clazz && Clazz._initClass(clazz,1,1,objThis);
    if ((!args || args.length == 0) && objThis.c$) {
    // allow for direct default call "new foo()" to run with its default
	// constructor
      objThis.c$.apply(objThis);
      args && (args[2] = inheritArgs)  
    }
    return;
  }

  // inner class
  
  // args[0] = outerObject
  // args[1] = b$ array
  // args[2-n] = actual arguments
  var outerObj = shiftArray(args, 0, 1);  
  var finalVars = shiftArray(args, 0, 1);
  var haveFinals = (finalVars || outerObj && outerObj.$finals$);
  if (!outerObj || !objThis)
    return;
  var clazz1 = (outerObj.__CLASS_NAME__ || outerObj instanceof String ? getClazz(outerObj) : null);
  (!clazz1 || clazz1 == outerObj) && (outerObj = objThis);

  if (haveFinals) {
    // f$ is short for the once-chosen "$finals$"
    var of$ = outerObj.$finals$;
    objThis.$finals$ = (finalVars ? 
      (of$ ? appendMap(appendMap({}, of$), finalVars) : finalVars)
      : of$ ? of$ : null);
  }
  // BH: For efficiency: Save the b$ array with the OUTER class as $b$,
  // as its keys are properties of it and can be used again.
  var b = outerObj.$b$;
  var isNew = false;
  var innerName = getClassName(objThis, true);
  if (!b) {
    b = outerObj.b$;
    // Inner class of an inner class must inherit all outer object references.
	// Note that this
    // can cause conflicts. For example, b$["java.awt.Component"] could refer to
	// the wrong
    // object if I did this wrong.
    // 
    if (!b) {
      // the outer class is not itself an inner class - start a new map
      b = {};
      isNew = true;
    } else if (b["$ " + innerName]) {
      // this inner class is already in the map pointing to a different
		// object. Clone the map.
      b = appendMap({},b);
      isNew = true;
    }
    b[getClassName(outerObj, true)] = outerObj;
    // add all superclass references for outer object
    clazz1 && addB$Keys(clazz1, isNew, b, outerObj, objThis);
  }
  var clazz2 = (clazz.superclazz == clazz1 ? null : clazz.superclazz || null);
  if (clazz2) {
		// we have an inner object that subclasses a different object
		// clone the map and overwrite with the correct values
      b = appendMap({},b);
	addB$Keys(clazz2, true, b, objThis, objThis);
  } else if (isNew) {
	// it is new, save this map with the OUTER object as $b$
	// 12018.12.20 but only if it is clean
	outerObj.$b$ = b;	
  }
  
  // final objective: save this map for the inner object
  // add a flag to disallow any other same-class use of this map.
  b["$ " + innerName] = 1;
  objThis.b$ = b;
  clazz.$this$0 && (objThis.this$0 = b[clazz.$this$0]);
  Clazz._initClass(clazz,1,0,objThis);
};


var fixBRefs = function(cl, obj, outerObj) {
	// see Clazz.super_
	obj.b$[cl.superclazz.$this$0] = outerObj;
}

var stripJavaLang = function(s) {
	return (
			s.indexOf("java.lang.") != 0 
			|| s == "java.lang.Object"
			|| s.length > 10 && !Character.isUpperCase$C(s.charAt(10)) ? 
					s :
					s.substring(10));
};

var addB$Keys = function(clazz, isNew, b, outerObj, objThis) {
  var cl = clazz;
  do {
    var key = getClassName(cl, true);
    if (!isNew && b[key])
      break;
    setB$key(key, b, outerObj);
  if (cl.implementz) {
  	var impl = cl.implementz;
  	for (var i = impl.length; --i >= 0;) {
      var key = getClassName(impl[i], true);
      if (isNew || !b[key]) {
    	setB$key(key, b, outerObj);
      }
  	}
  }
  } while ((cl = cl.superclazz));
};

var setB$key = function(key, b, outerObj) {
    b[key] = outerObj; 
    if (key.indexOf("java.lang.") == 0)
    	b[key.substring(10)] = outerObj;
    if (key == "javax.swing.JDialog")
    	b["java.awt.Dialog"] = outerObj;
    if (key == "javax.swing.JFrame")
    	b["java.awt.Frame"] = outerObj;
};

/**
 * // arg1 is the package name // arg2 is the full class name in quotes // arg3
 * is the class definition function, C$, which is called in Clazz.new_(). //
 * arg4 is the superclass // arg5 is the superinterface(s) // arg6 is the type:
 * anonymous(1), local(2), or absent
 */

Clazz.newInterface = function (prefix, name, f, _null2, interfacez, _0) {
  var c = Clazz.newClass(prefix, name, function(){}, null, interfacez, 0);
  f && f(c); // allow for j2sNative block
  return c;
};

var __allowOverwriteClass = true;

Clazz.newMeth = function (clazzThis, funName, funBody, modifiers) {

	if (!__allowOverwriteClass && clazzThis.prototype[funName]) 
		return;
	
	// modifiers: 1: static, 2: native, p3 -- private holder
  if (arguments.length == 1) {
    return Clazz.newMeth(clazzThis, 'c$', function(){
    	clazzThis.$load$ && Clazz.load(clazzThis,2);
    	Clazz.super_(clazzThis, this);
    	}, 1);
  }
  if (funName.constructor == Array) {
    // If funName is an array, we are setting aliases for generic calls.
    // For example: ['compareTo$S', 'compareTo$TK', 'compareTo$TA']
    // where K and A are generic types that are from a class<K> or class<A>
	// assignment.
    for (var i = funName.length; --i >= 0;)
      Clazz.newMeth(clazzThis, funName[i], funBody, modifiers);
    return;
  }
  
  var isStatic = (modifiers == 1 || modifiers == 2);
  var isPrivate = (typeof modifiers == "object");
  if (isPrivate) 
	clazzThis.$P$ = modifiers;
  Clazz.saemCount0++;
  funBody.exName = funName; // mark it as one of our methods
  funBody.exClazz = clazzThis; // make it traceable
  funBody.isPrivate = isPrivate;
  var f;
  if (isStatic || funName == "c$")
    clazzThis[funName] = funBody;
  if (clazzThis.$isInterface)
	clazzThis.$hasJava8Defaults = true;
  if (isPrivate && modifiers)
	modifiers[funName] = funBody;
  else 
	clazzThis.prototype[funName] = funBody;
  return funBody; // allow static calls as though they were not static
};

Clazz.newPackage = function (pkgName) {
  Clazz._Loader && Clazz._Loader.doTODO();
  if (Clazz.lastPackageName == pkgName || !pkgName)
    return Clazz.lastPackage;
  var pkgFrags = pkgName.split (/\./);
  var pkg = Clazz._allPackage;
  for (var i = 0; i < pkgFrags.length; i++) {
    var a = pkgFrags[i];
    if (!pkg[a]) {
      pkg[a] = {  __PKG_NAME__ : (pkg.__PKG_NAME__ ? pkg.__PKG_NAME__ + "." + a : a) }
      if (i == 0) {
    	setWindowValue(a, pkg[a]);
      }
    }
    pkg = pkg[a]
  }
  Clazz.lastPackageName = pkgName;
  return Clazz.lastPackage = pkg;
};

Clazz.super_ = function(cl, obj, outerObj) {
  if (outerObj) {
	// inner class is subclassing an inner class in another class using
	// OuterClass.super()
	fixBRefs(cl, obj, outerObj);
	return;
  }

  // implicit super() call
  
  if (cl.superclazz && cl.superclazz.c$) {
    // added [] here to account for the possibility of vararg default
	// constructor
    cl.superclazz.c$.apply(obj, [[]]);
  }
  cl.$init$ && cl.$init$.apply(obj);
}

// ///////////////////////////////////////////////////////////////////

var aas = "AAA";

var arrayClasses = {};

var arrayClass = function(baseClass, ndim) {
  ndim || (ndim = 1);
  var stub = Clazz._getParamCode(baseClass);
  var key = stub + ";" + ndim;
  var ret = arrayClasses[key];
  if (ret)
	return ret;
  while (aas.length < ndim)
    aas += aas;
  var aaa = aas.substring(0, ndim);
  var o = {};
  var a = new Array(ndim);
  o.arrayType = 1;
  o.__BASECLASS = baseClass;
  o.__NDIM = ndim;
  o.__CLASS_NAME__ = o.__ARRAYTYPE = stub + aaa;
  o.__COMPONENTTYPE = (o.__NDIM == 1 ? baseClass : null);
  var oclass = Clazz.getClass(o);
  oclass.getComponentType$ = function() { 
	if (!o.__COMPONENTTYPE)
		o.__COMPONENTTYPE = arrayClass(baseClass, ndim - 1);
    return (o.__COMPONENTTYPE.__PRIMITIVE 
    		|| o.__COMPONENTTYPE.$clazz$ ? o.__COMPONENTTYPE 
    		: Clazz.getClass(o.__COMPONENTTYPE)); 
  };
  oclass.getName$ = function() {return o.__NAME || (o__NAME = (function(stub) {
    switch (stub) {
    case "O":
      stub = "Object";
      break;
    case "H": // SwingJS -> Java
    	stub = "S";
    	break;
    case "S":
      stub = "String";
      break;
    default:
      if (stub.length > 1)
        stub = baseClass.__CLASS_NAME$__ || baseClass.__CLASS_NAME__;
      break;
    }
    if (stub.indexOf(".") >= 0)
      stub = "L" + stub + ";";
    else if (stub.length > 1)
      stub = "Ljava.lang." + stub + ";";
    return aaa.replace(/A/g,"[") + stub;
  })(stub))};
  arrayClasses[key] = oclass;
  return oclass;  
}


// var supportsNativeObject = window["j2s.object.native"]; // true


// Clazz.duplicatedMethods = {};

// Clazz._preps = {}; // prepareFields functions based on class name

// BH Clazz.getProfile monitors exactly what is being delegated with SAEM,
// which could be a bottle-neck for function calling.
// This is critical for performance optimization.

var __signatures = ""; 
var profilet0;
var _profileNew = null;
var _jsid0 = 0;

Clazz.startProfiling = function(doProfile) {
  _profileNew = {};
  if (typeof doProfile == "number") {
    _jsid0 = _jsid;
    setTimeout(function() { var s = "total wall time: " + doProfile + " sec\n" + Clazz.getProfile(); console.log(s); System.out.println(s)}, doProfile * 1000);
  } else if (doProfile === false) {
	_jsid = 0;
	_profileNew = null;
  }
  return (_profileNew ? "use Clazz.getProfile() to show results" : "profiling stopped and cleared")
}

var tabN = function(n) { n = ("" + n).split(".")[0]; return "..........".substring(n.length) + n + "\t" };

Clazz.getProfile = function() {
  var s = "run  Clazz.startProfiling() first";
    
    if (_profileNew) {
      s += "\n\n Total new objects: " + (_jsid - _jsid0) + "\n";
      s += "\ncount   \texec(ms)\n";
      s += "--------\t--------\t------------------------------\n";
      totalcount = 0;
      totaltime = 0;
      var rows = [];
      for (var key in _profileNew) {
        var count = _profileNew[key][0];
        var tnano = _profileNew[key][1];
        totalcount += count;
        totaltime += Math.abs(tnano);
        rows.push(tabN(count) + tabN(Math.round(tnano)) + "\t" +key + "\n");
      }
      rows.sort();
      rows.reverse();
      s += rows.join("");
      s+= tabN(totalcount)+tabN(Math.round(totaltime)) + "\n";
    }
  _profileNew = null;
  return s; // + __signatures;
}

var addProfileNew = function(c, t) {
  var s = c.__CLASS_NAME__ || c.__PARAMCODE;
  if (t < 0) {
	s += "[]";
	t = 0;
  }
  if (J2S._traceOutput && (s.indexOf(J2S._traceOutput) >= 0 || '"' + s + '"' == J2S._traceOutput)) {
    alert(s + "\n\n" + Clazz._getStackTrace());
    doDebugger();
  }

  var p = _profileNew[s]; 
  p || (p = _profileNew[s] = [0,0]);
  p[0]++;
  p[1]+=t;
}

// /////////////////// method creation ////////////////////////////////

var doDebugger = function() { debugger }

// /////////////////////// private supporting method creation
// //////////////////////

     
 var copyArrayProps = function(a, b) {
    b.__BYTESIZE = a.__BYTESIZE;
    b.__ARRAYTYPE = a.__ARRAYTYPE;
    b.__BASECLASS = a.__BASECLASS;
    b.__NDIM = a.__NDIM;
    b.getClass$ = a.getClass$; 
    b.equals$O = a.equals$O;
    b.hashCode$ = a.hashCode$;
    return b;
 }
 
 var aHCOffset = 500000000000
 var lHCOffset = 400000000000
 var iHCOffset = 300000000000
 var sHCOffset = 200000000000
 var bHCOffset = 100000000000


 var setArray = function(vals, baseClass, paramType, ndims) {
  ndims = Math.abs(ndims);
  vals.__JSID__ = ++_jsid;
  vals.getClass$ = function () { return arrayClass(this.__BASECLASS, this.__NDIM) };
  vals.hashCode$ = function() {return System.identityHashCode$O(this, aHCOffset);}
  vals.equals$O = function (a) {return this == a; } 

  vals.reallyEquals$O = function (a) { 
    if (!a || a.__ARRAYTYPE != this.__ARRAYTYPE || a.length != this.length)
      return false;
    if (a.length == 0)
    	return true;
    if (typeof a[0] == "object") {
      for (var i = a.length; --i >= 0;)
        if ((a[i] == null) != (this[i] == null) || a[i] != null 
          && (a[i].equals$O && !a[i].equals$O(this[i]) 
            || a.equals && !a[i].equals(this[i]) || a[i] !== this[i]))
          return false;
    } else {
    	for (var i = a.length; --i >= 0;)
            if (a[i] !== this[i])
              return false;
    }
    return true;  
  }; 
  
  vals.__ARRAYTYPE = paramType; // referenced in java.lang.Class
  vals.__BASECLASS = baseClass;
  vals.__NDIM = ndims;
  return vals;
}

/**
 * in-place shift of an array by k elements, starting with element i0, resetting
 * its length in case it is arguments (which does not have the .shift() method.
 * Returns a[i0]
 */
var shiftArray = function(a, i0, k) {
  if (a == null || k > a.length)
    return null;
  k || (k == 1);
  i0 || (i0 == 0);
  var arg = a[i0];
  for (var i = i0, n = a.length - k; i < n; i++)
    a[i] = a[i + k];
  a.length -= k;
  return arg;
};

var getParamCode = Clazz._getParamCode = function(cl) {
  cl.$clazz$ && (cl = cl.$clazz$);
  return cl.__PARAMCODE || (cl.__PARAMCODE = stripJavaLang(cl.__CLASS_NAME__).replace(/\./g, '_'));
}

var newTypedA = function(baseClass, args, nBits, ndims, isClone) {
  var dim = args[0];
  if (typeof dim == "string")
    dim = dim.charCodeAt(0); // int[] a = new int['\3'] ???
  var last = args.length - 1;
  var paramType = args[last];
  var val = args[last - 1];
  if (ndims < -1 || Math.abs(ndims) > 1) {
     //array of arrays;  -2: when x[30][]
    var xargs = new Array(last--); 
    for (var i = 0; i <= last; i++)
      xargs[i] = args[i + 1];
    // SAA -> SA
    xargs[last] = paramType.substring(0, paramType.length - 1);    
    var arr = new Array(dim);
    if (args[1] != null) {
        // arg[1] is null, we set the array type but do not fill in the array
    	// otherwise, call recursively
    	for (var i = 0; i < dim; i++) {    		 
    		arr[i] = newTypedA(baseClass, xargs, nBits, ndims - (ndims < 0 ? -1 : 1)); 
    	}
    }
  } else {
    // Clazz.newIntA(new int[5][] val = null
    // Clazz.newA(5 ,null, "SA") new String[5] val = null
    // Clazz.newA(-1, ["A","B"], "SA") new String[] val = {"A", "B"}
    // Clazz.newA(3, 5, 0, "IAA") new int[3][5] (second pass, so now args = [5,
	// 0, "IA"])
    if (val == null) {
      nBits = 0;
    } else if (nBits > 0 && dim < 0) {
      // make sure this is not a character
      for (var i = val.length; --i >= 0;)
        val[i].charAt && (val[i] = val[i].$c());
      dim = val; // because we can initialize an array using new
					// Int32Array([...])
    }
    if (nBits > 0)
      ndims = 1;
    var atype;
    // dim could be a number or an array
    switch (nBits) {
    case 8:
      var arr = new Int8Array(dim);
      break;
    case 16:
      var arr = new Int16Array(dim);
      break;
    case 32:
      var arr = new Int32Array(dim);
      break;
    case 64:
      var arr = (paramType != "JA" ? new Float64Array(dim) : typeof dim == "number" ? new Array(dim).fill(0) : dim);
      break;
    default:
      nBits = 0;
      var arr;
      if (isClone) {
        arr = new Array(dim = val.length);
      } else {
        arr = (dim < 0 ? val : new Array(dim));
        if (dim > 0 && val != null)
        	arr.fill(val);
      }
      break;
    }  
    arr.__BYTESIZE = arr.BYTES_PER_ELEMENT || (nBits >> 3);
  }
  return setArray(arr, baseClass, paramType, ndims);
}


/**
 * Return the class name of the given class or object.
 * 
 * @param clazzHost
 *            given class or object
 * @return class name
 */
var getClassName = function(obj, fAsClassName) {
  if (obj == null)
    return "NullObject";
  if (obj._NULL_)
    return obj.clazzName;
  switch(typeof obj) {
  case "number":
    return "n";
  case "boolean":
    return "b";
  case "string":
    // Always treat the constant string as String object.
    // This will be compatiable with Java String instance.
    return "String";
  case "function":
    if (obj.__CLASS_NAME__)
      return (fAsClassName ? obj.__CLASS_NAME__ : "Class"); // user defined
															// class name
    var s = obj.toString();
    var idx0 = s.indexOf("function");
    if (idx0 < 0)
      return (s.charAt(0) == '[' ? extractClassName(s) : s.replace(/[^a-zA-Z0-9]/g, ''));
    var idx1 = idx0 + 8;
    var idx2 = s.indexOf ("(", idx1);
    if (idx2 < 0)
      return "Object";
    s = s.substring (idx1, idx2);
    if (s.indexOf("Array") >= 0)
      return "Array"; 
    s = s.replace (/^\s+/, "").replace (/\s+$/, "");
    return (s == "anonymous" || s == "" ? "Function" : s);
  case "object":
    if (obj.__CLASS_NAME__) // user defined class name
      return obj.__CLASS_NAME__;
    if (!obj.constructor)
      return "Object"; // For HTML Element in IE
    if (!obj.constructor.__CLASS_NAME__) {
      if (obj.__VAL0__)
        return "Number";
      if (obj instanceof Boolean)
        return "Boolean";
      if (obj instanceof Array || obj.__BYTESIZE)
        return "Array";
      if (obj instanceof ReferenceError || obj instanceof TypeError) {
          // note that this is not technically the case.
    	  // we use this to ensure that try/catch delivers these as java.lang.Error instances
       	  return "Error";   	  
      }
      var s = obj.toString();
      // "[object Int32Array]"
      if (s.charAt(0) == '[')
        return extractClassName(s);
    }
    return getClassName(obj.constructor, true);
  }
  // some new, unidentified class
  return "Object";
};

var extractClassName = function(clazzStr) {
  // [object Int32Array]
  var clazzName = clazzStr.substring (1, clazzStr.length - 1);
  return (clazzName.indexOf("Array") >= 0 ? "Array" // BH -- for Float64Array
													// and Int32Array
    : clazzName.indexOf ("object ") >= 0 ? clazzName.substring (7) // IE
    : clazzName);
}

/**
 * Expand the shortened list of class names. For example: JU.Log, $.Display,
 * $.Decorations will be expanded to JU.Log, JU.Display, JU.Decorations where
 * "$." stands for the previous class name's package.
 * 
 * This method will be used to unwrap the required/optional classes list and the
 * ignored classes list.
 */
/* private */
var unwrapArray = function (arr) {
  if (!arr || arr.length == 0)
    return [];
  var last = null;
  for (var i = 0; i < arr.length; i++) {
    var ai = arr[i];
    if (typeof ai != "string")
      continue;
    if (ai.charAt(0) == '$') {
      if (ai.charAt(1) == '.') {
        if (!last)
          continue;
        var idx = last.lastIndexOf(".");
        if (idx != -1) {
          var prefix = last.substring (0, idx);
          arr[i] = prefix + ai.substring(1);
        }
      } else {
        arr[i] = "org.eclipse.s" + ai.substring (1);
      }
    }
    last = arr[i];
  }
  return arr;
};

/**
 * Return the JavaScript clazz of the given class or object.
 * 
 * @param clazzHost
 *            given class or object
 * @return class name
 */
var getClazz = function (clazzHost) {
  if (!clazzHost)
    return Clazz._O;  // null/undefined is always treated as Object
  if (typeof clazzHost == "function")
    return clazzHost;
  var clazzName;
  if (clazzHost._NULL_) {
    clazzName = clazzHost.clazzName;
  } else {
    switch (typeof clazzHost) {
    case "string":
      return String;
    case "object":
      if (!clazzHost.__CLASS_NAME__)
        return (clazzHost.constructor || Clazz._O);
      clazzName = clazzHost.__CLASS_NAME__;
    break;
    default:
      return clazzHost.constructor;
    }
  }
  return evalType(clazzName, true);
};

var appendMap = function(a, b) {
  if (b)
    for (var s in b) {
        a[s] = b[s];
    }
  return a;
}

var hashCode = 0;

var _jsid = 0;

// if (supportsNativeObject) { // true
  Clazz._O = function () {};
  Clazz._O.__CLASS_NAME__ = "Object";
  Clazz._O.__PARAMCODE = "O";
  Clazz._O.getClass$ = function () { return Clazz._O; }; 
// } else {
// Clazz._O = Object;
// }

/*
 * these methods are not part of Java.
 * 
 * var objMethods = { equals : function (o) { return this === o; }, hashCode :
 * function () { return this.__CLASS_NAME__.hashCode (); }, toString : function () {
 * return "class " + this.__CLASS_NAME__; } }; objMethods.equals$O =
 * objMethods.equals;
 */

// set object methods for Clazz._O and Array

  var addProto = function(proto, name, func) {
    func.exClazz = Clazz._O;
    func.exName = name;
    return proto[name] = func;
  };

// var minimalObjNames = [ "equals$", "equals$O", "hashCode$" /*"toString",*/ ];

;(function(proto) {

  addProto(proto, "equals$O", function (obj) {
    return this == obj;
  });

  addProto(proto, "hashCode$", function () {  
    return this._$hashcode || (this._$hashcode = ++hashCode)
  });

  addProto(proto, "getClass$", function () { return Clazz.getClass(this); });

  addProto(proto, "clone$", function () { return Clazz.clone(this); });

/*
 * Methods for thread in Object
 */
  addProto(proto, "finalize$", function () {});
  addProto(proto, "notify$", function () {});
  addProto(proto, "notifyAll$", function () {});
  addProto(proto, "wait$", function () {alert("Object.wait was called!" + arguments.callee.caller.toString())});
  addProto(proto, "toString$", Object.prototype.toString);
  addProto(proto, "toString", function () { return (this.__CLASS_NAME__ ? "[" + (this.__CLASS_NAME$__ || this.__CLASS_NAME__) + " object]" : this.toString$.apply(this, arguments)); });

})(Clazz._O.prototype);

var extendObjectMethodNames = [
  // all
  "equals$O", "getClass$", "clone$", "finalize$", "notify$", "notifyAll$", "wait$", 
  // not Number, Array
  "hashCode$", 
  // not String
  "toString" 
  ];

var EXT_NO_TOSTRING       = 1; // length - 1
var EXT_NO_HASHCODE       = 2; // length - 2

var extendObject = function(clazz, ext) {
  var op =Clazz._O.prototype;
  var cp = clazz.prototype;
  for (var i = extendObjectMethodNames.length - (ext || 0); --i >= 0;) {
    var p = extendObjectMethodNames[i];
    cp[p] = op[p];
  }
}

// see also
var excludeSuper = function(o) {
 return o == "b$" || o == "$this$0"
      || o == "$init$"
      || o == "$init0$"
      || o == "$static$"
      || o == "$defaults$"
      || o == "$clinit$"
      || o == "$classes$"
      || o == "$fields$"
      || o == "$load$"
      || o == "$Class$"
      || o == "$getMembers$"
      || o == "$getAnn$"
      || o == "prototype" 
      || o == "__PARAMCODE" 
      || o == "__CLASS_NAME__" 
      || o == "__CLASS_NAME$__" 
      || o == "superclazz"
      || o == "implementz"
      || o.startsWith("c$") 
}

var copyStatics = function(clazzFrom, clazzThis, isInterface) {
  for (var o in clazzFrom) {
    if (clazzThis[o] == undefined && !excludeSuper(o)) {
      clazzThis[o] = clazzFrom[o];
      if (isInterface)
        clazzThis.prototype[o] = clazzFrom[o];
    }
  }
  if (isInterface) {
	clazzFrom.$static$ && (initStatics(clazzFrom), clazzFrom.$static$());
	clazzThis.$defaults$ && clazzThis.$defaults$(clazzThis);
	for (var o in clazzFrom.prototype) {
	if (clazzThis.prototype[o] == undefined && !excludeSuper(o)) {
	clazzThis.prototype[o] = clazzFrom.prototype[o];
	}
	}
	if (clazzFrom.$defaults$) {
		__allowOverwriteClass = false;
		clazzFrom.$defaults$(clazzThis);
		__allowOverwriteClass = true;
	}
  }
}


var finalizeClazz = function(clazz, qname, bname, type, isNumber) {
  clazz.$isInterface = (type == 0);
  qname && (clazz.__CLASS_NAME__ = clazz.prototype.__CLASS_NAME__ = qname);
  bname && (clazz.__CLASS_NAME$__ = clazz.prototype.__CLASS_NAME$__ = bname);  // inner
																				// static
																				// classes
																				// use
																				// $
																				// not
																				// "."
  
  (type == 1) && (clazz.__ANON = clazz.prototype.__ANON = 1); 
  (type == 2) && (clazz.__LOCAL = clazz.prototype.__LOCAL = 1);
  
// if (!isNumber && type != 0)
// Clazz.newMeth(clazz, '$init0$', function(){var c;if ((c=clazz.superclazz) &&
// (c = c.$init0$))c.apply(this);}, 1);
  if (isNumber || type != 0)
	extendPrototype(clazz);

};

var extendPrototype = function(clazz, isPrimitive, addAll) {
  clazz.isInstance = function(o) { return Clazz.instanceOf(o, this) };
  var cp = clazz.prototype;
  var op = Clazz._O.prototype;        
  for (var i = 0; i < extendObjectMethodNames.length; i++) {
    var p = extendObjectMethodNames[i];
    if (!cp[p] || cp[p].exClazz == Clazz._O)
      addProto(cp, p, op[p]);
  }
}


Clazz.saemCount0 = 0 // methods defined

var NullObject = function () {};

var evalType = function (typeStr, isQualified) {
  if (typeStr == null)
    return null;
  var cl = (isQualified && Clazz._getDeclared(typeStr));
  if (cl)
    return cl;
  var idx = typeStr.lastIndexOf(".");
  if (idx >= 0) {
    var pkgName = typeStr.substring (0, idx);
    var pkg = Clazz.newPackage(pkgName);
    var clazzName = typeStr.substring (idx + 1);
    return pkg[clazzName];
  } 
  switch (typeStr) {
  case "string":
    return String;
  case "number":
    return Number;
  case "object":
    return Clazz._O;
  case "boolean":
    return Boolean;
  case "function":
    return Function;
  case "void":
  case "undefined":
  case "unknown":
    return typeStr;
  case "NullObject":
    return NullObject;
  default:
    return Clazz._getDeclared(typeStr);
  }
};

var equalsOrExtendsLevel = function (clazzThis, clazzAncestor) {
  while (true) {
    if (clazzThis == null)
      return false;  
    if (clazzThis === clazzAncestor)
      return true;
    if (clazzThis && clazzThis.implementz) {
      var impls = clazzThis.implementz;
      for (var i = impls.length; --i >= 0;)
        if (equalsOrExtendsLevel(impls[i], clazzAncestor))
          return true;
    }
    clazzThis = clazzThis.superclazz;
  }
  return false;
};

var knownInst = {};

var isInstanceOf = function (clazzTarget, clazzBase, isTgtStr, isBaseStr) {
  if (clazzTarget === clazzBase)
    return true;
  if (isTgtStr && ("void" == clazzTarget || "unknown" == clazzTarget))
    return false;
  if (isBaseStr && ("void" == clazzBase || "unknown" == clazzBase))
    return false;
  Clazz._initClass(clazzBase, 1)
  if (clazzTarget === (isTgtStr ? "NullObject" : NullObject)) {
    switch (clazzBase) {
    case "n":
    case "b":
      return false;
    case Number:
    case Boolean:
    case NullObject:
      break;
    default:
      return true;
    }
  } 
  var t = (isTgtStr ? clazzTarget : clazzTarget.__CLASS_NAME__ || clazzTarget.type);
  var b = (isBaseStr ? clazzBase : clazzBase.__CLASS_NAME__ || clazzBase.type);
  if (t && t == b)
	return true;
  var key = t + "|" + b;
  var val = knownInst[key];
  if (val)
	return (val == 1 ? true : false); 
  
  isTgtStr && (clazzTarget = Clazz._getDeclared(clazzTarget));
  isBaseStr && (clazzBase = Clazz._getDeclared(clazzBase));
  var ret = (clazzBase && clazzTarget && (
    clazzTarget == clazzBase 
      || clazzBase === Object 
      || clazzBase === Clazz._O
      || equalsOrExtendsLevel(clazzTarget, clazzBase)
    ));
if (t && b)
  knownInst[key] = (ret ? 1 : -1);
  return ret;
};


// ///////////////////////// Exception handling ////////////////////////////

/*
 * Use to mark that the Throwable instance is created or not.
 * 
 * Called from java.lang.Throwable, as defined in JSmolJavaExt.js
 * 
 * The underscore is important - it tells the JSmol ANT task to NOT turn this
 * into Clazz_initializingException, because coreBottom2.js does not include
 * that call, and so Google Closure Compiler does not minify it.
 * 
 */
/* public */
Clazz._initializingException = false;

/**
 * MethodException will be used as a signal to notify that the method is not
 * found in the current clazz hierarchy.
 */
/* private */
var MethodException = function () {
  this.toString = function () {
    return "j2s MethodException";
  };
};

var _isNPEExceptionPredicate;

;(function() { 
  /*
	 * sgurin: native exception detection mechanism. Only NullPointerException
	 * detected and wrapped to java excepions
	 */
  /**
	 * private utility method for creating a general regexp that can be used
	 * later for detecting a certain kind of native exceptions. use with error
	 * messages like "blabla IDENTIFIER blabla"
	 * 
	 * @param msg
	 *            String - the error message
	 * @param spliterName
	 *            String, must be contained once in msg spliterRegex String, a
	 *            string with the regexp literal for identifying the spitter in
	 *            exception further error messages.
	 */
  // reproduce NullPointerException for knowing how to detect them, and create
	// detector function Clazz._isNPEExceptionPredicate
  var $$o$$ = null;
  
  try {
    $$o$$.hello();
  } catch (e) {
    var _ex_reg = function(msg, spliterName, spliterRegex) {
      if(!spliterRegex) 
        spliterRegex="[^\\s]+";  
      var idx = msg.indexOf (spliterName), 
        str = msg.substring (0, idx) + spliterRegex + msg.substring(idx + spliterName.length), 
        regexp = new RegExp("^"+str+"$");
      return regexp;
    };
    if(/Opera[\/\s](\d+\.\d+)/.test(navigator.userAgent)) {// opera throws an
															// exception with
															// fixed messages
															// like "Statement
															// on line 23:
															// Cannot convert
															// undefined or null
															// to Object
															// Backtrace:
															// Line....long
															// text... "
      var idx1 = e.message.indexOf(":"), idx2 = e.message.indexOf(":", idx1+2);
      var _NPEMsgFragment = e.message.substr(idx1+1, idx2-idx1-20);
      _isNPEExceptionPredicate = function(e) { return e.message.indexOf(_NPEMsgFragment)!=-1; };
    }  else if(navigator.userAgent.toLowerCase().indexOf("webkit")!=-1) { // webkit,
																			// google
																			// chrome
																			// prints
																			// the
																			// property
																			// name
																			// accessed.
      var _exceptionNPERegExp = _ex_reg(e.message, "hello");
      _isNPEExceptionPredicate = function(e) { return _exceptionNPERegExp.test(e.message); };
    }  else {// ie, firefox and others print the name of the object accessed:
      var _exceptionNPERegExp = _ex_reg(e.message, "$$o$$");
      _isNPEExceptionPredicate = function(e) { return _exceptionNPERegExp.test(e.message); };
    }    
  };
})();

var getArgs = function(c) {
    var s = "";
    var args = c.arguments;
    for (var j = 0; j < args.length; j++) {
      var sa = (args[j] instanceof Object ? args[j].toString() : "" + args[j]);
      if (sa.length > 60)
        sa = sa.substring(0, 60) + "...";
      s += " args[" + j + "]=" + sa.replace(/\s+/g," ") + "\n";
    }
    return s;
}

var getSig = function(c, withParams) {
	var sig = (c.toString ? c.toString().substring(0, c.toString().indexOf("{")) : "<native method>");
    sig = " " + (c.exName ? c.exClazz.__CLASS_NAME__ + "." + c.exName  + sig.replace(/function /,""): sig) + "\n";
    if (withParams)
    	sig += getArgs(c);
    return sig;
}

Clazz._showStack = function(n) {
  if (!Clazz._stack)
	return;
  n && n < Clazz._stack.length || (n = Clazz._stack.length);
  if (!n)
	return;
  for (var i = 0; i < n; i++) {
	console.log("" + i + ":" + getSig(Clazz._stack[i], true));
  }	
  return "";
}

 
Clazz._getStackTrace = function(n) {
	Clazz._stack = [];
  // need to limit this, as JavaScript call stack may be recursive
  var haven = !!n
  haven || (n = 25);
  var showParams = (n < 0);
  if (showParams)
    n = -n;
  // updateNode and updateParents cause infinite loop here
  var estack = [];
  try {
	Clazz.failnow();
	} catch (e) {
  estack = e.stack.split("\n").reverse();
  estack.pop();
	}
  var s = "\n";
  try {
  var c = arguments.callee;
  for (var i = 0; i < n; i++) {
    if (!(c = c.caller))
      break;
    var sig = getSig(c, false);
    if (s.indexOf(sig) >= 0) {
    	s += "...";
    	break;
    } else {
    	Clazz._stack.push(c);
    	s += "" + i + sig;
        s += estack.pop() + "\n\n";
    }
    if (c == c.caller) {
      s += "<recursing>\n";
      break;
    }
    if (showParams) { 	
      s += getArgs(c);
    }
  }
  } catch(e){}  
  if (!haven)
	s += estack.join("\n");
  if (Clazz._stack.length) {
	s += "\nsee Clazz._stack";
	console.log("Clazz.stack = \n" + estack.join("\n"));
	console.log("Use Clazz._showStack() or Clazz._showStack(n) to show parameters");
  }
  return s;
}

// //////////////////////////////// package loading ///////////////////////

/*
 * all root packages. e.g. java.*, org.*, com.*
 */
Clazz._allPackage = {};


/**
 * Will be used to keep value of whether the class is defined or not.
 */
Clazz.allClasses = {};

Clazz.lastPackageName = null;
Clazz.lastPackage = null;

var unloadedClasses = [];

/**
 * used specifically for declaring prototypes using subclass.prototype = new
 * superclass(inheritArgs) without running a constructor or doing field
 * preparation.
 * 
 */ 
var inheritArgs = new (function(){return {"$J2SNOCREATE$":true}})();

// var _prepOnly = new (function(){return {"$J2SPREPONLY$":true}})();

/**
 * Inherit class with "extends" keyword and also copy those static members.
 * Example, as in Java, if NAME is a static member of ClassA, and ClassB extends
 * ClassA then ClassB.NAME can be accessed in some ways.
 * 
 * @param clazzThis
 *            child class to be extended
 * @param clazzSuper
 *            super class which is inherited from
 */
var setSuperclass = function(clazzThis, clazzSuper){

 clazzThis.superclazz = clazzSuper || Clazz._O;
  if (clazzSuper) {  
    copyStatics(clazzSuper, clazzThis, false);
    var p = clazzThis.prototype;
    if (clazzSuper == Number) {
      clazzThis.prototype = new Number();
    } else {
      clazzThis.prototype = new clazzSuper(inheritArgs);     
      if (clazzSuper == Error) {
        var pp = Throwable.prototype;
        for (o in pp) {
          if (!pp.exClazz || pp.exClazz != Clazz._O)
            clazzThis.prototype[o] = pp[o];
        }
      }
    } 
    for (o in p) {
      if (!p[o].exClazz || p[o].exClazz != Clazz._O)
      clazzThis.prototype[o] = p[o];
    }      
  }
  clazzThis.prototype.__CLASS_NAME__ = clazzThis.__CLASS_NAME__;
};

/**
 * Implementation of Java's keyword "implements". As in JavaScript there are on
 * "implements" keyword implemented, a property of "implementz" is added to the
 * class to record the interfaces the class is implemented.
 * 
 * @param clazzThis
 *            the class to implement
 * @param interfacez
 *            Array of interfaces
 */
var addInterface = function (clazzThis, interfacez) {
  if (interfacez instanceof Array) {
    for (var i = 0, n = interfacez.length; i < n; i++) {
      var iface = interfacez[i];
      if (iface instanceof Array) {
        var cl;
        for (var j = 0; j < iface.length; j++)
          cl = Clazz.load(iface[j]);
        iface = cl;
      }
      addInterface(clazzThis, iface);  
    }
    return;
  }
  // not an array...
  if (typeof interfacez == "string") {
    var str = interfacez;
    if (!(interfacez = Clazz.load(interfacez))) {
      alert("Missing interface: " + str);
      return;
    }
  }
  (clazzThis.implementz || (clazzThis.implementz = [])).push(interfacez);
  copyStatics(interfacez, clazzThis, true);
};


// //////////////////////// default package declarations
// ////////////////////////


/*
 * Check whether given package's classpath is setup or not. Only "java" and
 * "org.eclipse.swt" are accepted in argument.
 */
/* private */
var needPackage = function(pkg) {
  // note that false != null and true != null
  return (J2S.getGlobal(pkg + ".registered") && !classpathMap["@" + pkg]);
}

  // Make sure that packageClasspath ("java", base, true);
  // is called before any _Loader#loadClass is called.

  if (needPackage("java"))
    Clazz._Loader.loadPackage("java");

Clazz.newPackage("java.io");
Clazz.newPackage("java.lang.reflect");
Clazz.newPackage("java.util");


// NOTE: Any changes to this list must also be
// accounted for in net.sf.j2s.core.astvisitors.Java2ScriptVisitor.knownClasses
Clazz.newInterface(java.io,"Externalizable");
Clazz.newInterface(java.io,"Flushable");
Clazz.newInterface(java.io,"Serializable");
Clazz.newInterface(java.lang,"Cloneable");
Clazz.newInterface(java.lang,"Appendable");
Clazz.newInterface(java.lang,"Comparable");
Clazz.newInterface(java.lang,"Runnable");

	

;(function(){var P$=java.lang,p$1={},I$=[[0,'java.util.stream.StreamSupport','java.util.Spliterators','java.lang.CharSequence$lambda1','java.lang.CharSequence$lambda2',['java.lang.CharSequence','.LinesSpliterator']]]
,I$0=I$[0],$I$=function(i,n,m){return m?$I$(i)[n].apply(null,m):((i=(I$[i]||(I$[i]=Clazz.load(I$0[i])))),!n&&i.$load$&&Clazz.load(i,2),i)};

var C$=Clazz.newInterface(P$, "CharSequence");

C$.$classes$=[['LinesSpliterator',25]];

Clazz.newMeth(C$, 'lines$S',  function (s) {
	return $I$(1,"stream$java_util_Spliterator$Z",[Clazz.new_([s.getBytes$()],$I$(5,1).c$$BA), false]);
	}, 1);

;(function(){/*c*/var C$=Clazz.newClass(P$.CharSequence, "LinesSpliterator", function(){
	Clazz.newInstance(this, arguments[0],false,C$);
	}, null, 'java.util.Spliterator');

	C$.$clinit$=2;

	Clazz.newMeth(C$, '$init$', function () {
	},1);

	C$.$fields$=[['I',['index','fence'],'O',['value','byte[]']]]

	Clazz.newMeth(C$, 'c$$BA',  function (value) {
	C$.c$$BA$I$I.apply(this, [value, 0, value.length]);
	}, 1);

	Clazz.newMeth(C$, 'c$$BA$I$I',  function (value, start, length) {
	;C$.$init$.apply(this);
	this.value=value;
	this.index=start;
	this.fence=start + length;
	}, 1);

	Clazz.newMeth(C$, 'indexOfLineSeparator$I',  function (start) {
	for (var current=start; current < this.fence; current++) {
	var ch=this.value[current];
	if (ch == 10  || ch == 13  ) {
	return current;
	}}
	return this.fence;
	}, p$1);

	Clazz.newMeth(C$, 'skipLineSeparator$I',  function (start) {
	if (start < this.fence) {
	if (this.value[start] == 13 ) {
	var next=start + 1;
	if (next < this.fence && this.value[next] == 10  ) {
	return next + 1;
	}}return start + 1;
	}return this.fence;
	}, p$1);

	Clazz.newMeth(C$, 'next',  function () {
	var start=this.index;
	var end=p$1.indexOfLineSeparator$I.apply(this, [start]);
	this.index=p$1.skipLineSeparator$I.apply(this, [end]);
	return  String.instantialize(this.value, start, end - start);
	}, p$1);

	Clazz.newMeth(C$, 'tryAdvance$java_util_function_Consumer',  function (action) {
	if (action == null ) {
	throw Clazz.new_(Clazz.load('NullPointerException').c$$S,["tryAdvance action missing"]);
	}if (this.index != this.fence) {
	action.accept$O(p$1.next.apply(this, []));
	return true;
	}return false;
	});

	Clazz.newMeth(C$, 'forEachRemaining$java_util_function_Consumer',  function (action) {
	if (action == null ) {
	throw Clazz.new_(Clazz.load('NullPointerException').c$$S,["forEachRemaining action missing"]);
	}while (this.index != this.fence){
	action.accept$O(p$1.next.apply(this, []));
	}
	});

	Clazz.newMeth(C$, 'trySplit$',  function () {
	var half=(this.fence + this.index) >>> 1;
	var mid=p$1.skipLineSeparator$I.apply(this, [p$1.indexOfLineSeparator$I.apply(this, [half])]);
	if (mid < this.fence) {
	var start=this.index;
	this.index=mid;
	return Clazz.new_(C$.c$$BA$I$I,[this.value, start, mid - start]);
	}return null;
	});

	Clazz.newMeth(C$, 'estimateSize$',  function () {
	return this.fence - this.index + 1;
	});

	Clazz.newMeth(C$, 'characteristics$',  function () {
	return 1296;
	});

	Clazz.newMeth(C$);
	})()

C$.$defaults$ = function(C$){

Clazz.newMeth(C$, 'chars$', function () {
return $I$(1).intStream$java_util_function_Supplier$I$Z(((P$.CharSequence$lambda1||
(function(){var C$=Clazz.newClass(P$, "CharSequence$lambda1", function(){Clazz.newInstance(this, arguments[0],1,C$);}, null, 'java.util.function.Supplier', 1);

C$.$clinit$ = 1;

Clazz.newMeth(C$, '$init$', function () {}, 1);
/* lambda_E */
Clazz.newMeth(C$, 'get$', function () { return($I$(2).spliterator$java_util_PrimitiveIterator_OfInt$J$I(Clazz.new_(CharSequence$1CharIterator.$init$, [this, null]), this.b$['CharSequence'].length$(), 16));});
})()
), Clazz.new_($I$(3).$init$, [this, null])), 16464, false);
});

Clazz.newMeth(C$, 'codePoints$', function () {
return $I$(1).intStream$java_util_function_Supplier$I$Z(((P$.CharSequence$lambda2||
(function(){var C$=Clazz.newClass(P$, "CharSequence$lambda2", function(){Clazz.newInstance(this, arguments[0],1,C$);}, null, 'java.util.function.Supplier', 1);

C$.$clinit$ = 1;

Clazz.newMeth(C$, '$init$', function () {
}, 1);
/* lambda_E */
Clazz.newMeth(C$, 'get$', function () { return($I$(2).spliteratorUnknownSize$java_util_PrimitiveIterator_OfInt$I(Clazz.new_(CharSequence$1CodePointIterator.$init$, [this, null]), 16));});
})()
), Clazz.new_($I$(4).$init$, [this, null])), 16, false);
});
};;
(function(){var C$=Clazz.newClass(P$, "CharSequence$1CharIterator", function(){
Clazz.newInstance(this, arguments[0],true,C$);
}, null, [['java.util.PrimitiveIterator','java.util.PrimitiveIterator.OfInt']], 2);

C$.$clinit$ = 1;

Clazz.newMeth(C$, '$init0$', function () {
var c;if((c = C$.superclazz) && (c = c.$init0$))c.apply(this);
this.cur = 0;
}, 1);

Clazz.newMeth(C$, '$init$', function () {
this.cur = 0;
}, 1);

Clazz.newMeth(C$, 'hasNext$', function () {
return this.cur < this.b$['CharSequence'].length$();
});

Clazz.newMeth(C$, 'nextInt$', function () {
if (this.hasNext$()) {
return this.b$['CharSequence'].charAt$I.apply(this.b$['CharSequence'], [this.cur++]).$c();
} else {
throw Clazz.new_(Clazz.load('java.util.NoSuchElementException'));
}});

Clazz.newMeth(C$, ['forEachRemaining$java_util_function_IntConsumer','forEachRemaining$O'], function (block) {
for (; this.cur < this.b$['CharSequence'].length$(); this.cur++) {
block.accept$I(this.b$['CharSequence'].charAt$I(this.cur).$c());
}
});

Clazz.newMeth(C$);
})()
;
;
(function(){var C$=Clazz.newClass(P$, "CharSequence$1CodePointIterator", function(){
Clazz.newInstance(this, arguments[0],true,C$);
}, null, [['java.util.PrimitiveIterator','java.util.PrimitiveIterator.OfInt']], 2);

C$.$clinit$ = 1;

Clazz.newMeth(C$, '$init0$', function () {
var c;if((c = C$.superclazz) && (c = c.$init0$))c.apply(this);
this.cur = 0;
}, 1);

Clazz.newMeth(C$, '$init$', function () {
this.cur = 0;
}, 1);

Clazz.newMeth(C$, ['forEachRemaining$java_util_function_IntConsumer','forEachRemaining$O'], function (block) {
var length = this.b$['CharSequence'].length$();
var i = this.cur;
try {
while (i < length){
var c1 = this.b$['CharSequence'].charAt$I(i++);
if (!Character.isHighSurrogate$C(c1) || i >= length ) {
block.accept$I(c1.$c());
} else {
var c2 = this.b$['CharSequence'].charAt$I(i);
if (Character.isLowSurrogate$C(c2)) {
i++;
block.accept$I(Character.toCodePoint$C$C(c1, c2));
} else {
block.accept$I(c1.$c());
}}}
} finally {
this.cur=i;
}
});

Clazz.newMeth(C$, 'hasNext$', function () {
return this.cur < this.b$['CharSequence'].length$();
});

Clazz.newMeth(C$, 'nextInt$', function () {
var length = this.b$['CharSequence'].length$();
if (this.cur >= length) {
throw Clazz.new_(Clazz.load('java.util.NoSuchElementException'));
}var c1 = this.b$['CharSequence'].charAt$I.apply(this.b$['CharSequence'], [this.cur++]);
if (Character.isHighSurrogate$C(c1) && this.cur < length ) {
var c2 = this.b$['CharSequence'].charAt$I.apply(this.b$['CharSequence'], [this.cur]);
if (Character.isLowSurrogate$C(c2)) {
this.cur++;
return Character.toCodePoint$C$C(c1, c2);
}}return c1.$c();
});

Clazz.newMeth(C$);
})()
})();

// ////// (int) conversions //////////

// deprecated
Clazz.doubleToInt = Clazz.floatToInt = function (x) {
  // asm.js-style conversion
  return x|0;
};


// /////////////////////////////// Array additions
// //////////////////////////////
//
// BH: these are necessary for integer processing, especially
//
//

var arraySlice = function(istart, iend) {
  // could be Safari or could be fake
  istart || (istart = 0);
  iend || (iend = this.length);
  var b = new this.constructor(this.buffer.slice(istart * this.__BYTESIZE, iend * this.__BYTESIZE));
  b.__BYTESIZE = a.__BYTESIZE;
  b.__ARRAYTYPE = a.__ARRAYTYPE;
};

var setAType = function (IntXArray, nBytes, atype) {
  if (!IntXArray)
    alert("SwingJS will not work in this Browser")
  if (!IntXArray.prototype.sort)
    IntXArray.prototype.sort = Array.prototype.sort
  if (!IntXArray.prototype.slice)
    IntXArray.prototype.slice = function() {return arraySlice.apply(this, arguments)};
  IntXArray.prototype.clone$ = function() {
    return copyArrayProps(this, this.slice());
  };
}

setAType(Int8Array, 1, "BA");
setAType(Int16Array, 2, "HA");
setAType(Int32Array, 4, "IA");
setAType(Float64Array, 8, "DA");

java.lang.Object = Clazz._O;

// ////////////////////////// hotspot and unloading ////////////////////

// not implemented in SwingJS

// ////////////////////////// class loader /////////////////////////////

/*******************************************************************************
 * Copyright (c) 2007 java2script.org and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Zhou Renjian - initial API and implementation
 ******************************************************************************/
/*******************************************************************************
 * @author zhou renjian
 * @create July 10, 2006
 ******************************************************************************/

Clazz._Loader = function () {};

;(function(Clazz, _Loader) {

// The class loader is always accessed through Class.
// See Class.java for implementations of the methods of java.lang.ClassLoader
// such as getSystemResource and getResource

java.lang.ClassLoader = _Loader;
// BH windows-level only because it's java.lang
ClassLoader = _Loader;


_Loader.__CLASS_NAME__ = "ClassLoader";

Clazz.allClasses["java.lang.ClassLoader"] = _Loader;
_Loader.sysLoader = null;

_Loader.getSystemClassLoader$ = function() {
  return (_Loader.sysLoader ? _Loader.sysLoader : (_Loader.sysLoader = new Class().getClassLoader$()));
};


var assertionStatus = {};

_Loader.getSystemResource$S = function(name) {
	return _Loader.getSystemClassLoader$().getResource$(name);	
}

_Loader.getSystemResources$S = function(name) {
	return _Loader.getSystemClassLoader$().getResources$(name);	
}

_Loader.getSystemResourceAsStream$S = function(name) {
	return _Loader.getSystemClassLoader$().getResourceAsStream$(name);	
}

_Loader.getClassAssertionStatus$ = function(clazz) { // harmony
  var ret;
  var clazzName = clazz.__CLASS_NAME__ + ".";
  for (var c in assertionStatus) {
    if (clazzName.indexOf(c) == 0) {
      ret = assertionStatus[c];
      break;
    }
  }
  return (ret === false ? false : ret || Clazz.defaultAssertionStatus);
}

_Loader.prototype.hashCode$ = function(){return 1};


_Loader.prototype.getPackage$S = function (name) { return Clazz.new_(Clazz.load("java.lang.Package").c$$S$O, [name, Clazz._allPackage[name]]); };


_Loader.prototype.setDefaultAssertionStatus$Z = function(tf) {
  Clazz.defaultAssertionStatus = tf;
};

_Loader.prototype.clearAssertionStatus$ = function() {
  assertionStatus = {};
  Clazz.defaultAssertionStatus = false;
}

_Loader.prototype.setClassAssertionStatus$S$Z = _Loader.prototype.setPackageAssertionStatus$S$Z = function(clazzName, tf) {
  Clazz.allClasses[clazzName] && (Clazz.allClasses[clazzName].$_ASSERT_ENABLED_ = tf);
  assertionStatus[clazzName + "."] = tf;
};

_Loader.prototype.loadClass$S = function(clazzName) {
  return Clazz.forName(clazzName);
}


_Loader._checkLoad = J2S._checkLoad;
 
_Loader._TODO = [];

_Loader.doTODO = function() {
  while (_Loader._TODO.length) {
   var f = _Loader._TODO.shift();
   f();
    }
}
              
var loaders = [];

/* public */
_Loader.requireLoaderByBase = function (base) {
  for (var i = 0; i < loaders.length; i++) {
    if (loaders[i].$_$base == base) {
      return loaders[i];
    }
  }
  var loader = new _Loader ();
  loader.$_$base = base; 
  loaders.push(loader);
  return loader;
};

/**
 * 
 * Try to be compatible with Clazz system. In original design _Loader and Clazz
 * are independent! -- zhourenjian @ December 23, 2006
 */
var isClassdefined;
var definedClasses;

if (self.Clazz && Clazz._isClassDefined) {
  isClassDefined = Clazz._isClassDefined;
} else {
  definedClasses = {};
  isClassDefined = function (clazzName) {
    return definedClasses[clazzName] == true;
  };
}

/* private */
var classpathMap = Clazz.classpathMap = {};

/* public */
_Loader.loadPackageClasspath = function (pkg, base, isIndex, fSuccess, mode, pt) {
  var map = classpathMap;
  mode || (mode = 0);
  fSuccess || (fSuccess = null);
  pt || (pt = 0);

  /*
	 * In some situation, maybe, _Loader.packageClasspath ("java", ..., true);
	 * is called after other _Loader#packageClasspath, e.g. <code>
	 * _Loader.packageClasspath ("org.eclipse.swt", "...", true);
	 * _Loader.packageClasspath ("java", "...", true); </code> which is not
	 * recommended. But _Loader should try to adjust orders which requires
	 * "java" to be declared before normal _Loader #packageClasspath call before
	 * that line! And later that line should never initialize "java/package.js"
	 * again!
	 */
  var isPkgDeclared = (isIndex && map["@" + pkg]);
  if (mode == 0 && isIndex && !map["@java"] && pkg.indexOf ("java") != 0 && needPackage("java")) {
    _Loader.loadPackage("java", fSuccess ? function(_package){_Loader.loadPackageClasspath(pkg, base, isIndex, fSuccess, 1)} : null);
    if (fSuccess)
      return;
  }
  if (pkg instanceof Array) {
    unwrapArray(pkg);
    if (fSuccess) {
      if (pt < pkg.length)
        _Loader.loadPackageClasspath(pkg[pt], base, isIndex, function(_loadPackageClassPath){_Loader.loadPackageClasspath(pkg, base, isIndex, fSuccess, 1, pt + 1)}, 1);
      else
        fSuccess();
    } else {
      for (var i = 0; i < pkg.length; i++)
        _Loader.loadPackageClasspath(pkg[i], base, isIndex, null);
    }
    return;
  }
  switch (pkg) {
  case "java.*":
    pkg = "java";
    // fall through
  case "java":
    if (base) {
      // support ajax for default
      var key = "@net.sf.j2s.ajax";
      if (!map[key])
        map[key] = base;
      key = "@net.sf.j2s";
      if (!map[key])
        map[key] = base;
    }    
    break;
  case "swt":
    pkg = "org.eclipse.swt";
    break;
  case "ajax":
    pkg = "net.sf.j2s.ajax";
    break;
  case "j2s":
    pkg = "net.sf.j2s";
    break;
  default:
    if (pkg.lastIndexOf(".*") == pkg.length - 2)
      pkg = pkg.substring(0, pkg.length - 2);
    break;
  }
  if (base) // critical for multiple applets
    map["@" + pkg] = base;
  if (isIndex && !isPkgDeclared && !J2S.getGlobal(pkg + ".registered")) {
	  // the package idea has been deprecated
	  // the only package is core/package.js
    if (pkg == "java")
      pkg = "core" // JSmol -- moves java/package.js to core/package.js
    // not really asynchronous
    _Loader.loadClass(pkg + ".package", null, true, true, 1);
  }
  fSuccess && fSuccess();
};



/**
 * BH: allows user/developer to load classes even though wrapping and Google
 * Closure Compiler has not been run on the class.
 * 
 * Does initialize fully.
 * 
 * 
 * 
 */
Clazz.loadClass = function (name, onLoaded, async) {
  if (!self.Class) {
    Class = Clazz;
    Class.forName = Clazz.forName;
    // maybe more here
  }
  if (!name)
    return null;
  if (!async)
	return Clazz._4Name(name, null, null, true, true); 
  
  _Loader.loadClass(name, function() {
    var cl = Clazz._getDeclared(name);
    onLoaded(cl && Clazz._initClass(cl, 1, 1));
  }, true, async, 1);
  return true;

}

/**
 * Load the given class ant its related classes.
 */
/* public */
_Loader.loadClass = _Loader.prototype.loadClass = function (name, onLoaded, forced, async, mode) {
 
  mode || (mode = 0); // BH: not implemented
  (async == null) && (async = false);
  
   if (typeof onLoaded == "boolean")
    return evalType(name);

  // System.out.println("loadClass " + name)
  var path = _Loader.getClasspathFor(name);
  lastLoaded = name;
   Clazz.loadScript(path, name);
 }

/* private */
_Loader.loadPackage = function(pkg, fSuccess) {
  fSuccess || (fSuccess = null);
  J2S.setGlobal(pkg + ".registered", false);
  _Loader.loadPackageClasspath(pkg, 
    (_Loader.J2SLibBase || (_Loader.J2SLibBase = (_Loader.getJ2SLibBase() || "j2s/"))), 
    true, fSuccess);
};

/**
 * Register classes to a given *.z.js path, so only a single *.z.js is loaded
 * for all those classes.
 */
/* public */
_Loader.jarClasspath = function (jar, clazzes) {
  if (!(clazzes instanceof Array))
    clazzes = [clazzes];
  unwrapArray(clazzes);
  if (J2S._debugCore)
    jar = jar.replace(/\.z\./, ".")
  for (var i = clazzes.length; --i >= 0;) {
    clazzes[i] = clazzes[i].replace(/\//g,".").replace(/\.js$/g,"")
    classpathMap["#" + clazzes[i]] = jar;
  }
  classpathMap["$" + jar] = clazzes;
};

_Loader.setClasspathFor = function(clazzes) {
// Clazz._Loader.setClasspathFor("edu/colorado/phet/idealgas/model/PressureSensingBox.ChangeListener");
  if (!(clazzes instanceof Array))
    clazzes = [clazzes];
    for (var i = clazzes.length; --i >= 0;) {
      var path = clazzes[i];
      var jar = _Loader.getJ2SLibBase() + path.split(".")[0]+".js";
      path = path.replace(/\//g,".");
      classpathMap["#" + path] = jar;
      var a = classpathMap["$" + jar] || (classpathMap["$" + jar] = []);
      a.push(path);
    }
}


/**
 * Usually be used in .../package.js. All given packages will be registered to
 * the same classpath of given prefix package.
 */
/* public */
_Loader.registerPackages = function (prefix, pkgs) {
  // _Loader.checkInteractive ();
  var base = _Loader.getClasspathFor(prefix + ".*", true);
  for (var i = 0; i < pkgs.length; i++) {
      Clazz.newPackage(prefix + "." + pkgs[i]);
    _Loader.loadPackageClasspath(prefix + "." + pkgs[i], base);
  }

};

/**
 * Return the *.js path of the given class. Maybe the class is contained in a
 * *.z.js jar file.
 * 
 * @param clazz
 *            Given class that the path is to be calculated for. May be
 *            java.package, or java.lang.String
 * @param forRoot
 *            Optional argument, if true, the return path will be root of the
 *            given classs' package root path.
 * @param ext
 *            Optional argument, if given, it will replace the default ".js"
 *            extension.
 */
/* public */
_Loader.getClasspathFor = function (clazz, forRoot, ext) {
  var path = classpathMap["#" + clazz];
  if (!path || forRoot || ext) {
    var base;
    var idx;
    if (path) {
      clazz = clazz.replace(/\./g, "/");  
      if ((idx = path.lastIndexOf(clazz)) >= 0 
        || (idx = clazz.lastIndexOf("/")) >= 0 
          && (idx = path.lastIndexOf(clazz.substring(0, idx))) >= 0)
        base = path.substring(0, idx);
    } else {
      idx = clazz.length + 2;
      while ((idx = clazz.lastIndexOf(".", idx - 2)) >= 0)
        if ((base = classpathMap["@" + clazz.substring(0, idx)]))
          break;
      if (!forRoot)
        clazz = clazz.replace (/\./g, "/");  
    }
    if (base == null) {
      var bins = "binaryFolders";
      base = (Clazz[bins] && Clazz[bins].length ? Clazz[bins][0] 
        : _Loader[bins]  && _Loader[bins].length ? _Loader[bins][0]
        : "j2s");
    }
    path = (base.lastIndexOf("/") == base.length - 1 ? base : base + "/") + (forRoot ? ""
      : clazz.lastIndexOf("/*") == clazz.length - 2 ? clazz.substring(0, idx + 1)
      : clazz + (!ext ? ".js" : ext.charAt(0) != '.' ? "." + ext : ext));
  }    
  return path;// _Loader.multipleSites(path);
};

/**
 * page-customizable callbacks
 * 
 */
/* public */
_Loader.onScriptLoading = function (file){J2S._verbose && System.out.println("Classloader.onscriptloading " + file);};

/* public */
_Loader.onScriptLoaded = function (file, isError, data){};

/* public */
_Loader.onScriptInitialized = function (file){}; // not implemented

/* public */
_Loader.onScriptCompleted = function (file){}; // not implemented

/* public */
_Loader.onClassUnloaded = function (clazz){}; // not implemented

/* private */
var isClassExcluded = function (clazz) {
  return excludeClassMap["@" + clazz];
};

/* Used to keep ignored classes */
/* private */
var excludeClassMap = {};

Clazz._lastEvalError = null;

/* private */
var evaluate = function(file, js) {
  try {
	if (J2S._useEval)
		eval(js + ";//# sourceURL="+file)
	else
		new Function((J2S._strict ? '"use strict";':'')+js + ";//# sourceURL="+file)();
  } catch (e) {      
    var s = "[Java2Script] The required class file \n\n" + file + (js.indexOf("data: no") ? 
       "\nwas not found.\n"
      : "\ncould not be loaded. Script error: " + e.message + " \n\ndata:\n\n" + js) + "\n\n" 
      + (e.stack ? e.stack : Clazz._getStackTrace());
    Clazz._lastEvalError = s;    
    if (Clazz._isQuietLoad) 
      return;
    Clazz.alert(s);
    throw e;
  }
}

Clazz._initClass = function(c,clinit,statics,objThis) {
	var f;
	return clinit && (f=c.$clinit$) && (f === 1 || f === 2 ? Clazz.load(c,1) : f && typeof f == "function"? f() : 0),
	statics && c.$load$ && Clazz.load(c, 2),
	objThis  && (f=c.$init0$) && f.apply(objThis),
	c;
}

Clazz._getClassCount = function() {
	var n = 0;
	for (var c in Clazz.allClasses){n++};
	return n;
}

Clazz._4Name = function(clazzName, applet, state, asClazz, initialize, isQuiet) {
	// applet and state always null in SwingJS
  var cl;
  if (clazzName.indexOf("[") == 0) {
   cl = getArrayClass(clazzName);
   return (asClazz ? cl.$clazz$ : cl);
  }
  if (clazzName.indexOf(".") < 0)
    clazzName = "java.lang." + clazzName;  
  var isok = Clazz._isClassDefined(clazzName);
  if (isok && asClazz) {
    return Clazz._initClass(Clazz.allClasses[clazzName],1,1);
  } 
  if (!isok) {
    var name2 = null;
    var pt = clazzName.lastIndexOf("$");
    if (pt >= 0) {
      // BH we allow Java's java.swing.JTable.$BooleanRenderer as a stand-in
		// for java.swing.JTable.BooleanRenderer
      // when the static nested class is created using declareType
      name2 = clazzName.replace(/\$/g,".");
      if (Clazz._isClassDefined(name2)) {
        clazzName = name2;
      } else {
        cl = Clazz._4Name(clazzName.substring(0, pt), applet, state, true, initialize, isQuiet);
        cl && (clazzName = name2);	
        name2 = null;
      }
    }
    if (name2 == null) {
      var f = (J2S._isAsync && applet ? applet._restoreState(clazzName, state) : null);
      if (f == 1)
        return null; // must be already being created
      if (_Loader.setLoadingMode(f ? _Loader.MODE_SCRIPT : "xhr.sync")) {
        _Loader.loadClass(clazzName, f, false, true, 1);
        return null; // this will surely throw an error, but that is OK
      }
      // alert ("Using Java reflection: " + clazzName + " for " + applet._id +
		// " \n"+ Clazz._getStackTrace());
      _Loader.loadClass(clazzName);
    }    
  }
  cl = evalType(clazzName);
  if (!cl){
	if (isQuiet || Clazz._isQuietLoad)
		return null;
    alert(clazzName + " could not be loaded");
    doDebugger();
  }
  Clazz._setDeclared(clazzName, cl);
  // note triple== here
  Clazz._initClass(cl, initialize !== false, initialize === true);
  return (asClazz ? cl : Clazz.getClass(cl));
};

// BH: possibly useful for debugging
Clazz.currentPath= "";


Clazz.loadScript = function(file, nameForList) {

  Clazz.currentPath = file;
  // loadedScripts[file] = true;
  // also remove from queue
  // removeArrayItem(classQueue, file);

  var file0 = file;
  if (J2S._nozcore) {
    file = file.replace(/\.z\.js/,".js");
  }
  var data = "";
  try{
    _Loader.onScriptLoading(file);
    data = J2S.getFileData(file);
    evaluate(file, data);
    if (nameForList)
    	Clazz.ClassFilesLoaded.push(nameForList.replace(/\./g,"/") + ".js");
    _Loader.onScriptLoaded(file, null, data);
  }catch(e) {
	Clazz.ClassFilesLoaded.pop();
    _Loader.onScriptLoaded(file, e, data);
    var s = ""+e;
    if (data.indexOf("Error") >= 0)
      s = data;
    if (s.indexOf("missing ] after element list")>= 0)
      s = "File not found";
    if (file.indexOf("/j2s/core/") >= 0) {
      System.out.println(s + " loading " + file);
    } else {
     alert(s + " loading file " + file + "\n\n" + e.stack);
      doDebugger()
    
    }
  }
}

/**
 * Used in package /* public
 */
var runtimeKeyClass = _Loader.runtimeKeyClass = "java.lang.String";

/* private */
var J2sLibBase;

/**
 * Return J2SLib base path from existed SCRIPT src attribute.
 */
/* public */
_Loader.getJ2SLibBase = function () {
  var o = J2S.getGlobal("j2s.lib");
  return (o ? o.base + (o.alias == "." ? "" : (o.alias ? o.alias : (o.version ? o.version : "1.0.0")) + "/") : null);
};

/**
 * Indicate whether _Loader is loading script synchronously or asynchronously.
 */
/* private */
var isAsynchronousLoading = true;

/* private */
var isUsingXMLHttpRequest = false;

/* private */
var loadingTimeLag = -1;

_Loader.MODE_SCRIPT = 4;
_Loader.MODE_XHR = 2;
_Loader.MODE_SYNC = 1;

// Integer mode: Script 4; XHR 2; SYNC bit 1;
// async is currently ignored

/* public */
_Loader.setLoadingMode = function (mode, timeLag) {
  var async = true;
  var ajax = true;
  if (typeof mode == "string") {
    mode = mode.toLowerCase();
    if (mode.indexOf("script") >= 0)
      ajax = false;
    else
      async = (mode.indexOf("async") >=0);
    async = false; // BH
  } else {
    if (mode & _Loader.MODE_SCRIPT)
      ajax = false;
    else
      async = !(mode & _Loader.MODE_SYNC);
  }
  isUsingXMLHttpRequest = ajax;  // ignored
  isAsynchronousLoading = async;  // ignored
  loadingTimeLag = (async && timeLag >= 0 ? timeLag: -1); // ignored
  return async; // will be false
};

/*
 * Load those key *.z.js. This *.z.js will be surely loaded before other queued
 * *.js.
 */
/* public */
_Loader.loadZJar = function (zjarPath, keyClass) {
// used only by package.js for core.z.js
  var f =  null;
  var isArr = (keyClass instanceof Array);
  if (isArr)
    keyClass = keyClass[keyClass.length - 1];
// else
  // f = (keyClass == runtimeKeyClass ? runtimeLoaded : null);
  _Loader.jarClasspath(zjarPath, isArr ? keyClass : [keyClass]);
  // BH note: runtimeKeyClass is java.lang.String
  _Loader.loadClass(keyClass, null, true);
};

Clazz.binaryFolders =  _Loader.binaryFolders = [ _Loader.getJ2SLibBase() ];

})(Clazz, Clazz._Loader);

// }
/*******************************************************************************
 * Copyright (c) 2007 java2script.org and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Zhou Renjian - initial API and implementation
 ******************************************************************************/
/*******************************************************************************
 * @author zhou renjian
 * @create Jan 11, 2007
 ******************************************************************************/

Clazz._LoaderProgressMonitor = {};

;(function(CLPM, J2S) {

var fadeOutTimer = null;
var fadeAlpha = 0;
var monitorEl = null;
var lastScrollTop = 0;
var bindingParent = null;

CLPM.DEFAULT_OPACITY = (J2S && J2S._j2sLoadMonitorOpacity ? J2S._j2sLoadMonitorOpacity : 55);

/* public */
CLPM.hideMonitor = function () {
    monitorEl.style.display = "none";
}

/* public */
CLPM.showStatus = function (msg, fading) {
  if (!monitorEl) {
    createHandle ();
    if (!attached) {
      attached = true;
      // Clazz.addEvent (window, "unload", cleanup);
      // window.attachEvent ("onunload", cleanup);
    }
  }
  clearChildren(monitorEl);
  if (msg == null) {
    if (fading) {
      fadeOut();
    } else {
      CLPM.hideMonitor();
    }
    return;
  }
  
  monitorEl.appendChild(document.createTextNode ("" + msg));
  if (monitorEl.style.display == "none") {
    monitorEl.style.display = "";
  }
  setAlpha(CLPM.DEFAULT_OPACITY);
  var offTop = getFixedOffsetTop();
  if (lastScrollTop != offTop) {
    lastScrollTop = offTop;
    monitorEl.style.bottom = (lastScrollTop + 4) + "px";
  }
  if (fading) {
    fadeOut();
  }
};

/* private static */ 
var clearChildren = function (el) {
  if (!el)
    return;
  for (var i = el.childNodes.length; --i >= 0;) {
    var child = el.childNodes[i];
    if (!child)
      continue;
    if (child.childNodes && child.childNodes.length)
      clearChildren (child);
    try {
      el.removeChild (child);
    } catch (e) {};
  }
};
/* private */ 
var setAlpha = function (alpha) {
  if (fadeOutTimer && alpha == CLPM.DEFAULT_OPACITY) {
    window.clearTimeout (fadeOutTimer);
    fadeOutTimer = null;
  }
  fadeAlpha = alpha;
  // monitorEl.style.filter = "Alpha(Opacity=" + alpha + ")";
  monitorEl.style.opacity = alpha / 100.0;
};
/* private */ 
var hidingOnMouseOver = function () {
  CLPM.hideMonitor();
};

/* private */ 
var attached = false;
/* private */ 
var cleanup = function () {
  // if (monitorEl) {
  // monitorEl.onmouseover = null;
  // }
  monitorEl = null;
  bindingParent = null;
  // Clazz.removeEvent (window, "unload", cleanup);
  // window.detachEvent ("onunload", cleanup);
  attached = false;
};
/* private */ 
var createHandle = function () {
  var div = document.createElement ("DIV");
  div.id = "_Loader-status";
  div.style.cssText = "position:absolute;bottom:4px;left:4px;padding:2px 8px;"
      + "z-index:" + (J2S.getGlobal("j2s.lib").monitorZIndex || 10000) + ";background-color:#8e0000;color:yellow;" 
      + "font-family:Arial, sans-serif;font-size:10pt;white-space:nowrap;";
  div.onmouseover = hidingOnMouseOver;
  monitorEl = div;
  if (bindingParent) {
    bindingParent.appendChild(div);
  } else {
    document.body.appendChild(div);
  }
  return div;
};
/* private */ 

var fadeOut = function () {
  if (monitorEl.style.display == "none") return;
  if (fadeAlpha == CLPM.DEFAULT_OPACITY) {
    fadeOutTimer = window.setTimeout(function () {
          fadeOut();
        }, 750);
    fadeAlpha -= 5;
  } else if (fadeAlpha - 10 >= 0) {
    setAlpha(fadeAlpha - 10);
    fadeOutTimer = window.setTimeout(function () {
          fadeOut();
        }, 40);
  } else {
    monitorEl.style.display = "none";
  }
};
/* private */
var getFixedOffsetTop = function (){
  if (bindingParent) {
    var b = bindingParent;
    return b.scrollTop;
  }
  var dua = navigator.userAgent;
  var b = document.body;
  var p = b.parentNode;
  var pcHeight = p.clientHeight;
  var bcScrollTop = b.scrollTop + b.offsetTop;
  var pcScrollTop = p.scrollTop + p.offsetTop;
  return (dua.indexOf("Opera") < 0 && document.all ? (pcHeight == 0 ? bcScrollTop : pcScrollTop)
    : dua.indexOf("Gecko") < 0 ? (pcHeight == p.offsetHeight 
        && pcHeight == p.scrollHeight ? bcScrollTop : pcScrollTop) : bcScrollTop);
};

// if (window["ClazzLoader"]) {
// _Loader.onScriptLoading = function(file) {
// CLPM.showStatus("Loading " + file + "...");
// };
// _Loader.onScriptLoaded = function(file, isError) {
// CLPM.showStatus(file + (isError ? " loading failed." : " loaded."), true);
// };
// _Loader.onGlobalLoaded = function(file) {
// CLPM.showStatus("Application loaded.", true);
// };
// _Loader.onClassUnloaded = function(clazz) {
// CLPM.showStatus("Class " + clazz + " is unloaded.", true);
// };
// }

})(Clazz._LoaderProgressMonitor, J2S);

/*******************************************************************************
 * Copyright (c) 2007 java2script.org and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Zhou Renjian - initial API and implementation
 ******************************************************************************/
/*******************************************************************************
 * @author zhou renjian
 * @create Nov 5, 2005
 ******************************************************************************/

Clazz.Console = {};

;(function(Con) {
/**
 * Setting maxTotalLines to -1 will not limit the console result
 */
Con.maxTotalLines =  10000;

Con.setMaxTotalLines = function (lines) {
  Con.maxTotalLines = (lines > 0 ? lines : 999999);
}

Con.maxLatency = 40;

Con.setMaxLatency = function (latency) {
  Con.maxLatency = (latency > 0 ? latency : 40);
};

Con.pinning  = false;

Con.enablePinning = function (enabled) {
  Con.pinning = enabled;
};

Con.linesCount = 0;

Con.metLineBreak = false;


/*
 * Give an extension point so external script can create and bind the console
 * themself.
 * 
 */
Con.createConsoleWindow = function (parentEl) {
  var console = document.createElement ("DIV");
  console.style.cssText = "font-family:monospace, Arial, sans-serif;";
  document.body.appendChild (console);
  return console;
};

var c160 = String.fromCharCode(160); // nbsp;
c160 += c160+c160+c160;

Con.consoleOutput = function (s, color) {
  var con = consoleDiv;
  if (con && typeof con == "string")
    con = consoleDiv = document.getElementById(con)
  if (!con) {
    return false; // BH this just means we have turned off all console action
  }
   if (con == window.console) {
    if (color == "red")
      con.error(s);
    else
      con.log(s);
    return;
  }

	if (s == '\0') {
	con.innerHTML = "";
	con.lineCount = 0;
	return;
	}
   
  if (Con.linesCount > Con.maxTotalLines) {
    for (var i = 0; i < 1000; i++) {
      if (con && con.childNodes.length > 0) {
        con.removeChild(con.childNodes[0]);
      }
    }
    Con.linesCount = Con.maxTotalLines - 1000;
  }

  var willMeetLineBreak = false;
  s = (typeof s == "undefined" ? "" : s == null ? "null" : "" + s);
  s = s.replace (/\t/g, c160);
  if (s.length > 0)
    switch (s.charAt(s.length - 1)) {
    case '\n':
    case '\r':
      s = (s.length > 1 ? s.substring (0, s.length - (s.charAt(s.length - 2) == '\r' ? 2 : 1)) : "");
      willMeetLineBreak = true;
      break;
    }

  var lines = null;
  s = s.replace (/\t/g, c160);
  lines = s.split(/\r\n|\r|\n/g);
  for (var i = 0, last = lines.length - 1; i <= last; i++) {
    var lastLineEl = null;
    if (Con.metLineBreak || Con.linesCount == 0 
        || con.childNodes.length < 1) {
      lastLineEl = document.createElement ("DIV");
      con.appendChild (lastLineEl);
      lastLineEl.style.whiteSpace = "nowrap";
      Con.linesCount++;
    } else {
      try {
        lastLineEl = con.childNodes[con.childNodes.length - 1];
      } catch (e) {
        lastLineEl = document.createElement ("DIV");
        con.appendChild (lastLineEl);
        lastLineEl.style.whiteSpace = "nowrap";
        Con.linesCount++;
      }
    }
    var el = document.createElement ("SPAN");
    lastLineEl.appendChild (el);
    el.style.whiteSpace = "nowrap";
    if (color)
      el.style.color = color;
    var l = lines[i]
    if (l.length == 0)
      l = c160;
    el.appendChild(document.createTextNode(l));
    if (!Con.pinning)
      con.scrollTop += 100;
    Con.metLineBreak = (i != last || willMeetLineBreak);
  }

  var cssClazzName = con.parentNode.className;
  if (!Con.pinning && cssClazzName
      && cssClazzName.indexOf ("composite") != -1) {
    con.parentNode.scrollTop = con.parentNode.scrollHeight;
  }
  Con.lastOutputTime = new Date ().getTime ();
};

/*
 * Clear all contents inside the console.
 */
/* public */
Con.clear = function () {
  try {
    Con.metLineBreak = true;
    var console = consoleDiv;
    if (console == window.console || !console || typeof console == "string" && !(console = document.getElementById (console)))
      return;
    console.innerHTML = "";
    Con.linesCount = 0;
  } catch(e){};
};

/* public */
Clazz.alert = function (s) {
  Con.consoleOutput (s + "\r\n");
};

})(Clazz.Console);

var getURIField = function(name, def) {
	try {
		var ref = document.location.href.toLowerCase();
		var i = ref.indexOf(name.toLowerCase() + "=");
		if (i >= 0)
			def = (document.location.href + "&").substring(
					i + name.length + 1).split("&")[0];
	} catch (e) {
	} finally {
		return def;
	}
}

Clazz._setDeclared("java.lang.System", java.lang.System = System = {});
;(function(C$){

C$.lineSeparator = "\n";
C$.props = null;
	
C$.setIn$java_io_InputStream=function ($in) {
	C$.$in=$in;
}

C$.setOut$java_io_PrintStream=function (out) {
	C$.out=out;
	out.println = out.println$S;
	out.print = out.print$S;
}

C$.setErr$java_io_PrintStream=function (err) {
	C$.err=err;
	err.println = err.println$S;
	err.print = err.print$S;
}

C$.console$=function () {
	return null;
}

C$.inheritedChannel$=function () {
	return null;
}

C$.setSecurityManager$SecurityManager=function (s) {
}

C$.getSecurityManager$=function () {
	return null;
}

C$.currentTimeMillis$=function () {
	{
	return new Date().getTime();
}
}

C$.nanoTime$=function () {
	{
	return Math.round(window.performance.now() * 1e6);
}
}

C$.arraycopy$O$I$O$I$I=function (src, srcPos, dest, destPos, length) {

	if (src !== dest || srcPos > destPos) { for (var i = length; --i >= 0;) dest[destPos++] = src[srcPos++]; } else { destPos += length; srcPos += length; for (var i = length; --i >= 0;) src[--destPos] = src[--srcPos]; }
}

C$.identityHashCode$O=function (x, offset) {
	return x==null ? 0 : x._$hashcode || (typeof x == "string" ? x.hashCode$() : (x._$hashcode = ++hashCode + (offset || 0)));
}

C$.getProperties$=function () {
	if (C$.props == null )
		C$.props=Clazz.new_("java.util.Properties");
	for (a in sysprops)
		C$.props.put$O$O(a, sysprops[a]);
	return C$.props;
}

C$.lineSeparator$=function () {
	return C$.lineSeparator;
}

C$.setProperties$java_util_Properties=function (props) {
	C$.props = props;
}

C$.getProperty$S=function (key) {
	if (key == "java.awt.headless")
		return Clazz._isHeadless;
	C$.checkKey$S(key);
	var p = (C$.props == null ? sysprops[key] : C$.props.getProperty$S(key))
	return (p == null ? null : p);
}

C$.getProperty$S$S=function (key, def) {
	C$.checkKey$S(key);
	if (C$.props == null) {
		var prop = sysprops[key];
		return (prop == null ? def : prop);
	}
	return C$.props.getProperty$S$S(key, def);
}

C$.setProperty$S$S=function (key, value) {
	C$.checkKey$S(key);
	var ret;
	if (C$.props == null) {
		ret = sysprops[key];
		sysprops[key] = value;
		return ret || null;
	}
	return C$.props.setProperty$S$S(key, value);
}

C$.clearProperty$S=function (key) {
	C$.checkKey$S(key);
	return (C$.props == null ? null : C$.props.remove$O(key));
}

C$.checkKey$S=function (key) {
	if (key == null ) {
	throw Clazz.new_(Clazz.load('NullPointerException').c$$S,["key can\'t be null"]);
	}if (key.equals$O("")) {
	throw Clazz.new_(Clazz.load('IllegalArgumentException').c$$S,["key can\'t be empty"]);
	}
}

C$.getenv$S=function (name) {
	var s = J2S.getGlobal(name) || getURIField(name, null);
	return s || null;
}

var env = null;

C$.getenv$=function () {
	return env || (env = Clazz.load("java.util.Properties"));
}



C$.exit$I=function (status) {
	Clazz.loadClass("java.lang.Runtime").getRuntime$().exit$I(status | 0);
}

C$.gc$=C$.runFinalization$=C$.runFinalizersOnExit$Z=C$.load$S=C$.loadLibrary$S=C$.mapLibraryName$S=
	function (libname) {return null;}

var fixAgent = function(agent) {return "" + ((agent = agent.split(";")[0]),
		(agent + (agent.indexOf("(") >= 0 && agent.indexOf(")") < 0 ? ")" : ""))) }

	var agent = navigator.userA;
	var sysprops = {
			"file.separator" : "/",
			"line.separator" : "\n",
			"java.awt.printerjob" : "swingjs.JSPrinterJob",
			"java.class.path" : "/",
			"java.class.version" : "80",
			"java.home" : "https://.",
			"java.vendor" : "java2script/SwingJS/OpenJDK",
			"java.vendor.url" : "https://github.com/BobHanson/java2script",
			"java.version" : "1.8",
			"java.vm.name":"Java SwingJS",
			"java.vm.version" : "1.8",
			"java.specification.version" : "1.8",
			"java.io.tmpdir" : J2S.getGlobal("j2s.tmpdir"),
			"os.arch" : navigator.userAgent,
			"os.name" : fixAgent(navigator.userAgent).split("(")[0],
			"os.version": fixAgent(navigator.appVersion).replace(fixAgent(navigator.userAgent), ""),
			"path.separator" : ":",
			"user.dir" : "/TEMP/swingjs",
			"user.home" : "/TEMP/swingjs",
			"user.name" : "swingjs",
			"javax.xml.datatype.DatatypeFactory" : "swingjs.xml.JSJAXBDatatypeFactory",
			"javax.xml.bind.JAXBContextFactory" : "swingjs.xml.JSJAXBContextFactory"	
	}


})(System);

;(function(Con, Sys) {

Sys.exit$ = Sys.exit$I;

Sys.out = new Clazz._O ();
Sys.out.__CLASS_NAME__ = "java.io.PrintStream";
Sys.err = new Clazz._O ();
Sys.err.__CLASS_NAME__ = "java.io.PrintStream";

var checkTrace = function(s) {
	if (J2S._nooutput || !J2S._traceFilter && !J2S._traceOutput) return;
	if (J2S._traceFilter) {
		if ((s= "" + s).indexOf(J2S._traceFilter) < 0) 
			return;
	} else if (!(s = "" + s) || s.indexOf(J2S._traceOutput) < 0 && '"' + s + '"' != J2S._traceOutput) {
		return;
	}
	alert(s + "\n\n" + Clazz._getStackTrace());
	doDebugger();
}

var setps = function(ps, f) {

ps.flush$ = function() {}

ps.print = ps.print$ = ps.print$O = ps.print$Z = ps.print$I = ps.print$S = ps.print$C = function (s) { 
  checkTrace(s);
  f("" + s);
};

ps.print$J = function(l) {ps.print(Long.$s(l))}
ps.print$F = ps.print$D = function(f) {
	var s = "" + f; 
	ps.println(s.indexOf(".") < 0 && s.indexOf("Inf") < 0 ? s + ".0" : s);
}

ps.printf = ps.printf$S$OA = ps.format = ps.format$S$OA = function (f, args) {
  ps.print(String.format$S$OA.apply(null, arguments));
}

ps.println = ps.println$ = ps.println$Z = ps.println$I = ps.println$S = ps.println$C = ps.println$O = function(s) {
 checkTrace(s);
 f((s && s.toString ? s.toString() : "" + s)  + "\r\n");
};

ps.println$J = function(l) {ps.println(Long.$s(l))}
ps.println$F = ps.println$D = function(f) {
	var s = "" + f; 
	ps.println(s.indexOf(".") < 0 && s.indexOf("Inf") < 0 ? s + ".0" : s);
}

ps.write$I = function(ch) {
  ps.print(String.fromCharCode(ch));	
}

ps.write$BA = function (buf) {
	ps.write$BA$I$I(buf, 0, buf.length);
};

ps.write$BA$I$I = function (buf, offset, len) {
  ps.print(String.instantialize(buf, offset, len));
};

}

setps(Sys.out, function(s) {Con.consoleOutput(s)});
setps(Sys.err, function(s) {Con.consoleOutput(s, "red")});

})(Clazz.Console, System);


Clazz._Loader.registerPackages("java", [ "io", "lang", "lang.reflect", "util" ]);


// old J2S.setGlobal("java.registered", true);

// /////////////// special definitions of standard Java class methods
// ///////////

var C$, m$ = Clazz.newMeth;

Clazz._setDeclared("java.lang.Math", java.lang.Math = Math);

Math.rint || (Math.rint = function(a) {
 var b;
 return Math.round(a) + ((b = a % 1) != 0.5 && b != -0.5 ? 0 : (b = Math.round(a % 2)) > 0 ? b - 2 : b);
});

// Java 1.8

Math.abs$J = function(x) { return Long.$sign(x) < 0 ? Long.$neg(x) : Long.$dup(x); }

Math.max$J$J = function(x,y) { return Long.max$J$J(x,y); }

Math.min$J$J = function(x,y) { return Long.min$J$J(x,y); }

Math.round$D = function(x) { return Clazz.toLong(Math.round(x)); }

var arex = function(s) {
	throw Clazz.new_(Clazz.load('ArithmeticException').c$$S,[s||"integer overflow"]);
}

Math.addExact$J$J = function(x, y) {
    var r = Long.$add(x,y);
    (Long.$sign(r) != 0 && Long.$sign(x) == Long.$sign(y) && Long.$sign(x) != Long.$sign(r)) && arex();
    return r;
}

Math.subtractExact$J$J = function(x, y) {
    var r = Long.$sub(x,y);
    (Long.$sign(r) != 0 && Long.$sign(x) == Long.$sign(y) && Long.$sign(x) != Long.$sign(r)) && arex();
    return r;
}

Math.floorDiv$J$J = function(x,y) { 
	var r = Long.$div(x,y);
	return (r < 0 && Long.$ne(Long.$mul(r,y), x) ? Long.$inc(r, -1) : r);
}

Math.floorMod$J$J = function(x,y) { return Long.$sub(x, Long.$mul(Math.floorDiv(x, y), y)); }

Math.incrementExact$J = function(a) {
    (Long.$eq(a, Long_MAX_VALUE)) && arex();
    return Long.$inc(a,1);
}

Math.decrementExact$J = function(a) {
    (Long.$eq(a, Long_MIN_VALUE)) && arex();
    return Long.$inc(a,-1);
}

Math.multiplyExact$J$J = function(x, y) {
    var r = Long.$mul(x,y);
    if (Long.$sign(r) != Long.$sign(x) * Long.$sign(y)) {
    	arex();
    }
    return r;
}

Math.negateExact$J = function(a) {return Long.$neg(a);}

Math.toIntExact$J = function(value) {
    if (!Long.$eq(Long.$ival(value), value)) {
    	arex();
    }
    return value;
}

Math.addExact = function(x, y) {
    var r = x + y;
    // HD 2-12 Overflow iff both arguments have the opposite sign of the result
    if (r > Integer.MAX_VALUE || r < Integer.MIN_VALUE) {
    	arex();
    }
    return r;
}


Math.subtractExact = function(x, y) {
    var r = x - y;
    // HD 2-12 Overflow iff the arguments have different signs and
    // the sign of the result is different than the sign of x
    if (r > Integer.MAX_VALUE || r < Integer.MIN_VALUE) {
    	arex();
    }
    return r;
}

Math.multiplyExact = function(x, y) {
	var r = x * y;
    if (r > Integer.MAX_VALUE || r < Integer.MIN_VALUE) {
    	arex();
    }
    return r;
}
Math.incrementExact = function(a) {
    if (a == Integer.MAX_VALUE) {
    	arex();
    }
    return a + 1;
}

Math.decrementExact = function(a) {
    if (a == Integer.MIN_VALUE) {
    	arex();
    }
    return a - 1;
}

Math.negateExact = function(a) {return -a}

Math.floorDiv || (Math.floorDiv = function(x,y) { 
    var r = (x / y) | 0;
    if ((x ^ y) < 0 && (r * y != x)) {
        r--;
    }
    return r;
})

Math.floorMod || (Math.floorMod = function(x,y) { return x - Math.floorDiv(x, y) * y; })

// 
Math.log10||(Math.log10=function(a){return Math.log(a)/Math.E});

Math.hypot||(Math.hypot=function(x,y){return Math.sqrt(Math.pow(x,2)+Math.pow(y,2))});

Math.toDegrees||(Math.toDegrees=function(angrad){return angrad*180.0/Math.PI;});

Math.toRadians||(Math.toRadians=function(angdeg){return angdeg/180.0*Math.PI});

Math.copySign||(Math.copySign=function(mag,sign){return((sign>0?1:-1)*Math.abs(mag))});

// could use Math.sign(), but this was used to preserve cross-brower
// compatability (not in Internet Explorer)
Math.signum||(Math.signum=function(d){return(d==0.0||isNaN(d))?d:d < 0 ? -1 : 1});

Math.scalb||(Math.scalb=function(d,scaleFactor){return d*Math.pow(2,scaleFactor)});

Math.nextAfter||
(Math.nextAfter=function(start,direction){
    if (isNaN(start) || isNaN(direction))
    	return NaN;
    if (direction == start)
    	return start;
    if (start == Double.MAX_VALUE && direction == Double.POSITIVE_INFINITY)
    	return Double.POSITIVE_INFINITY;
    if (start == -Double.MAX_VALUE && direction == Double.NEGATIVE_INFINITY)
    	return Double.NEGATIVE_INFINITY;
    if (start == Double.POSITIVE_INFINITY && direction == Double.NEGATIVE_INFINITY)
    	return Double.MAX_VALUE;
    if (start == Double.NEGATIVE_INFINITY && direction == Double.POSITIVE_INFINITY)
    	return -Double.MAX_VALUE;
    if (start == 0) 
    	return (direction > 0 ? Double.MIN_VALUE : -Double.MIN_VALUE);

	geta64()[0] = start;
	var i0 = i64[0];
	var i1 = i64[1];
	var carry;
	if ((direction > start) == (start >= 0)) {
		i64[0]++;
		carry = (i64[0] == 0 ? 1 : 0);
	} else {
		i64[0]--;
		carry = (i64[0] == 4294967295 ? -1 : 0);
	} 
	if (carry)
		i64[1]+=carry;
	return a64[0];
});

Math.nextAfter$D$D = Math.nextAfter;

Math.nextAfter$F$D =function(start,direction){
    if (isNaN(start) || isNaN(direction))
    	return NaN;
    if (direction == start)
    	return start;
    if (start == Float.MAX_VALUE && direction == Float.POSITIVE_INFINITY)
    	return Float.POSITIVE_INFINITY;
    if (start == -Float.MAX_VALUE && direction == Float.NEGATIVE_INFINITY)
    	return Float.NEGATIVE_INFINITY;
    if (start == Float.POSITIVE_INFINITY && direction == Float.NEGATIVE_INFINITY)
    	return Float.MAX_VALUE;
    if (start == Float.NEGATIVE_INFINITY && direction == Float.POSITIVE_INFINITY)
    	return -Float.MAX_VALUE;
    if (start == 0 && direction < 0)
    	return -Float.MIN_VALUE;
    if (start == 0) 
    	return (direction > 0 ? Float.MIN_VALUE : -Float.MIN_VALUE);
    
    geta32()[0] = start;
	i32[0] += ((direction > start) == (start >= 0) ? 1 : -1); 
	return a32[0];
};


Math.nextUp||(Math.nextUp=function(d){ return Math.nextAfter(d, Double.POSITIVE_INFINITY); });

Math.nextUP$D=Math.nextUp;

Math.nextUp$F = function(f){ return Math.nextAfter$F$D(f, Double.NEGATIVE_INFINITY); };


Math.nextDown||(Math.nextDown=function(d){ return Math.nextAfter(d, Double.NEGATIVE_INFINITY); });

Math.nextDown$D=Math.nextDown;

Math.nextDown$F = function(f){ return Math.nextAfter$F$D(f, Double.NEGATIVE_INFINITY); };


Math.ulp||(Math.ulp=function(d){
        if (isNaN(d)) {
            return Double.NaN;
        } 
        if (isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        } 
        if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
            return Math.pow(2, 971);
        }
        return Math.nextUp(Math.abs(d));
});

Math.ulp$D = Math.ulp;

Math.ulp$F = function(f){
    if (isNaN(f)) {
        return Float.NaN;
    } 
    if (isInfinite(f)) {
        return Float.POSITIVE_INFINITY;
    } 
    if (f == Float.MAX_VALUE || f == -Float.MAX_VALUE) {
        return Math.pow(2, 104);
    }
    return Math.nextUp$F(Math.abs(f));
};

Math.getExponent = Math.getExponent$D = function(d) {
	geta64()[0] = d;
    return ((i64[1] & 0x7ff00000) >> 20) - 1023;
};

Math.getExponent$F=function(f){
    return ((Float.floatToRawIntBits$F(f) & 0x7f800000) >> 23) - 127;
}

Math.IEEEremainder||(Math.IEEEremainder=function (x, y) {
	if (Double.isNaN$D(x) || Double.isNaN$D(y) || Double.isInfinite$D(x) || y == 0) 
		return NaN;
	if (!Double.isInfinite$D(x) && Double.isInfinite$D(y))
		return x;
	var modxy = x % y;
	if (modxy == 0) return modxy;
	var rem = modxy - Math.abs(y) * Math.signum(x);
	if (Math.abs(rem) == Math.abs(modxy)) {
		var div = x / y;
		return (Math.abs(Math.round(div)) > Math.abs(div) ? rem : modxy);
	}
	return (Math.abs(rem) < Math.abs(modxy) ? rem : modxy);
});


Clazz._setDeclared("java.lang.Number", java.lang.Number=Number);
Number.prototype._numberToString=Number.prototype.toString;
  extendObject(Array, EXT_NO_HASHCODE);
  extendObject(Number, EXT_NO_HASHCODE);
Number.__CLASS_NAME__="Number";
addInterface(Number,java.io.Serializable);
// extendPrototype(Number, true, false);
Number.prototype.compareTo$ = Number.prototype.compareTo$Number = 
	Number.prototype.compareTo$O = Number.prototype.compareTo$Byte = Number.prototype.compareTo$Integer = 
	Number.prototype.compareTo$Short = Number.prototype.compareTo$Float = Number.prototype.compareTo$Double = 
						function(x) { var a = this.valueOf(), b = x.valueOf(); return (a < b ? -1 : a == b ? 0 : 1) };

var $b$ = new Int8Array(1);
var $s$ = new Int16Array(1);
var $i$ = new Int32Array(1);

// short forms, for the actual numbers in JavaScript
m$(Number,["byteValue"],function(){return ($b$[0] = this, $b$[0]);});
m$(Number,["shortValue"],function(){return ($s$[0] = this, $s$[0]);});
m$(Number,["intValue"],function(){return ($i$[0] = this, $i$[0]);});
m$(Number,["longValue"],function(){return Clazz.toLong(this);});

// Object values
m$(Number,["byteValue$"],function(){return this.valueOf().byteValue();});
m$(Number,["shortValue$"],function(){return this.valueOf().shortValue();});
m$(Number,["intValue$"],function(){return this.valueOf().intValue();});
m$(Number,["longValue$"],function(){return this.valueOf().longValue();});
m$(Number,["floatValue$", "doubleValue$"],function(){return this.valueOf();});
m$(Number,["longValue$"],function(){return this.valueOf().longValue();});
m$(Number,["$incr$"],function(n){return this.$box$(this.valueOf() + n);});
m$(Number,["$mul$"],function(v){return this.$box$(this.valueOf() * v);});
m$(Number,["$neg$"],function(v){return this.$box$(-this.valueOf());});
m$(Number,["$inv$"],function(v){return this.$box$(~this.valueOf());});
m$(Number,["$c"],function(v){return this.valueOf();});

Clazz.incrAN = function(A,i,n,isPost) {
	var v = A[i];
	A[i] = (v.TYPE ? v.$incr$(n) : Long.$inc(v,n));
	return (isPost ? v : A[i]);
}

Clazz._setDeclared("java.lang.Integer", java.lang.Integer=Integer=function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
});

var primTypes = {};

var FALSE = function() { return false };
var EMPTY_CLASSES = function() {return Clazz.array(Class, [0])};
var NULL_FUNC = function() {return null};

var setJ2STypeclass = function(cl, type, paramCode) {
// TODO -- should be a proper Java.lang.Class
  primTypes[paramCode] = cl;
  cl.TYPE = {
    isPrimitive$: function() { return true },
    type:type, 
    __PARAMCODE:paramCode, 
    __PRIMITIVE:1  // referenced in java.lang.Class
  };
  cl.TYPE.isArray$ = cl.TYPE.isEnum$ = cl.TYPE.isAnnotation$ = FALSE;
  cl.TYPE.toString = cl.TYPE.getName$ = cl.TYPE.getTypeName$ 
    = cl.TYPE.getCanonicalName$ = cl.TYPE.getSimpleName$ = function() {return type};
  cl.TYPE.isAssignableFrom$Class = (function(t) {return function(c) {return c == t}})(cl.TYPE);
  cl.TYPE.getSuperclass$ = cl.TYPE.getComponentType$ = NULL_FUNC;
  cl.TYPE.getInterfaces$ = EMPTY_CLASSES;
}

var decorateAsNumber = function (clazz, qClazzName, type, PARAMCODE, hcOffset) {
  clazz.prototype.valueOf=function(){return 0;};
  clazz.prototype.__VAL0__ = 1;
  if (hcOffset)
	clazz.prototype.hashCode$ = function() {return this.valueOf() + hcOffset};
  finalizeClazz(clazz, qClazzName, null, 0, true);
  extendPrototype(clazz, true, true);
  setSuperclass(clazz, Number);
  addInterface(clazz, Comparable);
  setJ2STypeclass(clazz, type, PARAMCODE);
  return clazz;
};

Clazz.toLong = function(v) {
	if (typeof v == "string") {
		v = parseInt(v);
		if (isNaN(v))
			return 0;
	}
	return (v.length ? v : v >= minLong && v <= maxLong ? v - v%1 :  v == Infinity ? LONG_MAX_VALUE : v == -Infinity ? LONG_MIN_VALUE : isNaN(v) ? 0 : toLongRMS(v - v%1));
}

var parseIntLimit = function(s,radix, min, max) {
	var v = (s == null || s.indexOf(".") >= 0 || s.startsWith("0x") ? NaN : radix === false ? parseInt(s) : parseInt(s, radix));
	if (!isNaN(v)) {
		// check for trailing garbage
		var v1 = parseInt(s + "1", radix);
		if (v1 == v)
			v = NaN;
	}
	if (isNaN(v) || v < min || v > max){
		throw Clazz.new_(NumberFormatException.c$$S, ["parsing " + s + " radix " + radix]);
	}
	return v;
}

decorateAsNumber(Integer, "Integer", "int", "I", iHCOffset);

Integer.toString=Integer.toString$I=Integer.toString$I$I=Integer.prototype.toString=function(i,radix){
	switch(arguments.length) {
	case 2:
		return i.toString(radix);
	case 1:
		return "" +i;
	case 0:
		return (this===Integer ? "class java.lang.Integer" : ""+this.valueOf());
	}
};

var minInt = Integer.MIN_VALUE=Integer.prototype.MIN_VALUE=-0x80000000;
var maxInt = Integer.MAX_VALUE=Integer.prototype.MAX_VALUE=0x7fffffff;
Integer.SIZE=Integer.prototype.SIZE=32;



var ints = [];
var longs = [];
var shorts = [];
var chars = {};

var minValueOf = -128;
var maxValueOf = 127;

var getCachedNumber = function(i, a, cl, c$) {
  if (a == chars) {	
	return a[i] ? a[i] : (a[i] = Clazz.new_(cl[c$], [i]));
  }
  if (i >= minValueOf && i <= maxValueOf) {
	var v = a[i - minValueOf];
	return (v ? v : a[i - minValueOf] = Clazz.new_(cl[c$], [i])); 
  }
}

m$(Integer,"c$", function(v){ // SwingJS only -- for new Integer(3)
	v || v == null || (v = 0);
	if (typeof v != "number")
		v = Integer.parseInt$S$I(v, 10);
	v = v.intValue();  
	this.valueOf=function(){return v;};
	}, 1);

m$(Integer, "c$$S", function(v){
	v = Integer.parseInt$S$I(v, 10);
	this.valueOf=function(){return v;};
	}, 1);

m$(Integer, "c$$I", function(v){
 this.valueOf=function(){return v;};
}, 1);

m$(Integer,"valueOf$S",
function(s){
	return Integer.valueOf$S$I(s, 10);
}, 1);

m$(Integer,"valueOf$S$I",
function(s, radix){
  return Integer.valueOf$I(Integer.parseInt$S$I(s, radix));
}, 1);

m$(Integer,"valueOf$I",
function(i){
  v |= 0;
  var v = getCachedNumber(i, ints, Integer, "c$$I");
  return (v ? v : Clazz.new_(Integer.c$$I, [i]));
}, 1);

m$(Integer,"parseInt$S",
function(s){
	return parseIntLimit(s, false, minInt, maxInt);
}, 1);

m$(Integer,"parseInt$S$I",
function(s,radix){
	return parseIntLimit(s, radix, minInt, maxInt);
}, 1);

m$(Integer,"getInteger$S$I",
function(ms,i){
  return Integer.valueOf$I(i);
}, 1);

m$(Integer,"highestOneBit$I",
	function(i) { 
	i |= (i >>  1);
	i |= (i >>  2);
	i |= (i >>  4);
	i |= (i >>  8);
	i |= (i >> 16);
	return i - (i >>> 1);
	}, 1);

m$(Integer,"lowestOneBit$I",
		function(i) { return i & -i;}, 1);

m$(Integer,"rotateLeft$I$I",
		function(i, distance) { return (i << distance) | (i >>> -distance); }, 1);

m$(Integer,"rotateRight$I$I",
		function(i, distance) { return (i >>> distance) | (i << -distance); }, 1);

m$(Integer,"reverse$I",
	function(i) { 
	i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
	i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
	i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
	i = (i << 24) | ((i & 0xff00) << 8) |
	((i >>> 8) & 0xff00) | (i >>> 24);
    return i;}, 1);

m$(Integer,"reverseBytes$I",
	function(i) { 
		return ((i >>> 24)           ) |
	((i >>   8) &   0xFF00) |
	((i <<   8) & 0xFF0000) |
	((i << 24));
	}, 1);

m$(Integer,"signum$I", function(i){ return i < 0 ? -1 : i > 0 ? 1 : 0; }, 1);

m$(Integer,"bitCount$I",
	function(i) {
	i = i - ((i >>> 1) & 0x55555555);
	i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
	i = (i + (i >>> 4)) & 0x0f0f0f0f;
	i = i + (i >>> 8);
	i = i + (i >>> 16);
	return i & 0x3f;
	}, 1);

m$(Integer,"numberOfLeadingZeros$I",
	function(i) {
	if (i == 0) return 32;
	var n = 1;
	if (i >>> 16 == 0) { n += 16; i <<= 16; }
	if (i >>> 24 == 0) { n +=  8; i <<=  8; }
	if (i >>> 28 == 0) { n +=  4; i <<=  4; }
	if (i >>> 30 == 0) { n +=  2; i <<=  2; }
	n -= i >>> 31;
	return n;
	}, 1);

m$(Integer,"numberOfTrailingZeros$I",
	function(i) {
	if (i == 0) return 32;
	var n = 31;
	var y = i <<16; if (y != 0) { n = n - 16; i = y; }
	y = i << 8; if (y != 0) { n = n - 8; i = y; }
	y = i << 4; if (y != 0) { n = n - 4; i = y; }
	y = i << 2; if (y != 0) { n = n - 2; i = y; }
	return n - ((i << 1) >>> 31);
	}, 1);

m$(Integer,"equals$O",
function(s){
return (s instanceof Integer) && s.valueOf()==this.valueOf();
});

m$(Integer, "$box$", function(v) {
	return Integer.valueOf$I(v);
});

Integer.toHexString$I=function(d){
if (d < 0) {
var b = d & 0xFFFFFF;
var c = ((d>>24)&0xFF);
return c._numberToString(16) + (b = "000000" + b._numberToString(16)).substring(b.length - 6);
}
return d._numberToString(16);
};
Integer.toOctalString$I=function(d){return d._numberToString(8);};
Integer.toBinaryString$I=function(d){return d._numberToString(2);};

Integer.toUnsignedLong$I=function(x){return (x > 0 ? x : x + 0x100000000);};
Integer.toUnsignedString$I=function(x){return "" + Integer.toUnsignedLong$I(x);};
Integer.toUnsignedString$I$I=function(x,r){return Long.toString$J(Integer.toUnsignedLong$I(x),r);};

m$(Integer,"decodeRaw$S", function(n){
if (n.indexOf(".") >= 0)n = "";
var i = (n.startsWith("-") ? 1 : 0);
n = n.replace(/\#/, "0x").toLowerCase();
var radix=(n.startsWith("0x", i) ? 16 : n.startsWith("0", i) ? 8 : 10);
// The general problem with parseInt is that is not strict --
// ParseInt("10whatever") == 10.
// Number is strict, but Number("055") does not work, though ParseInt("055", 8)
// does.
// need to make sure negative numbers are negative
if (n == "" || radix == 10 && isNaN(+n))
	return NaN
n = (+n) & 0xFFFFFFFF;
return (radix == 8 ? parseInt(n, 8) : n);
}, 1);

m$(Integer,"decode$S", function(n){
  if (isNaN(n = Integer.decodeRaw$S(n)) || n < Integer.MIN_VALUE|| n > Integer.MAX_VALUE)
    throw Clazz.new_(NumberFormatException.c$$S,["Invalid Integer"]);
  return Clazz.new_(Integer.c$$I, [n]);
}, 1);



// Note that Long is problematic in JavaScript

Clazz._setDeclared("java.lang.Long", java.lang.Long=Long=function(){
	this.c$(arguments[0]);
});

decorateAsNumber(Long, "Long", "long", "J", lHCOffset);

Long.toString=Long.toString$J=Long.toString$J$I = Long.prototype.toString=function(i, radix){
	switch(arguments.length) {
	case 2:
		return (i.length ? Long.$s(i,radix) : i.toString(radix));
	case 0:
		if (this===Long)
			return "class java.lang.Long";
		i = this.valueOf();
		break;
	}
	return (i.length ? Long.$s(i) : "" + i);
};

	
// 64-bit long methods
// RMS [R 16 bits, M 47+1 bits, S (1/0/-1)] (for storage)
// RLH [R 16 bits, ML 24 bits, MH 24+1 bits] (for bit, * / % ops)

var JSSAFE = 9007199254740991; // can +/-1
var RBITS = 24; 
var LBITS = RBITS;
var HBITS = (64 - LBITS - RBITS); // 16
var MAXR = 1 << RBITS;
var RMASK = MAXR - 1;
var MAXL = 1 << LBITS;
var LMASK = MAXL - 1;
var MAXH = 1 << HBITS;
var HMASK = MAXH - 1;
var HSIGNB = MAXH >> 1; // 0x8000;
var MAXM = MAXL*MAXH;
var T15 = 10**15; 
var T8 = 10**8; 
var T15RD = T15/MAXR; // 5.9604644775390625
var T15RN = T15RD|0;
var T15RF = T15RD - T15RN;
var MSIGNB = 0x8000000000; // Java long min >>>24;549755813888
var MMINMAX = 0x10000000000; // Java overflow >>>24;
var LONG_MAX_VALUE = [16777215,549755813887,1];
var LONG_MIN_VALUE = [0,549755813888,-1];

var parseLong = function(s, radix, v) {
	if (v >= -JSSAFE && v <= JSSAFE)
		return v;
	var isNeg = (s.charAt(0) == '-');
	if (isNeg || s.charAt(0) == '+')
		s = s.substring(1);
	var n = s.length;
	var m = 0,r = 0;
	switch (radix) {
	case 10:
		return toLongRMS((isNeg ? "-" : "") + s,1);
	case 2:
		if (n > 64)
			return null;
		m = parseInt(s.substring(0, n - RBITS), 2); 
		r = parseInt(s.substring(n - RBITS), 2);
		break;
	case 8:
		if (n > 33)
			return null;
		// mmmmmmmmmmmmmmmmm rrrrrrrr
		m = parseInt(s.substring(0, n - RBITS/3), 8); 
		r = parseInt(s.substring(n - RBITS/3), 8); 
		break;
	case 16: 
		if (n > 16)
			return null;
		// mmmmmmmmmmmm rrrrrr
		m = parseInt(s.substring(0, n - RBITS/4), 16); 
		r = parseInt(s.substring(n - RBITS/4), 16);
		break;
	case 16: 
		if (n > 8)
			return null;
		// mmmmmmmmmmmm rrrrrr
		m = parseInt(s.substring(0, n - RBITS/8), 32); 
		r = parseInt(s.substring(n - RBITS/8), 32);
		break;
	default:
		return null;
	}
	if (m >= 0) {
		if (m >= MSIGNB) {
			if (m > MSIGNB || r != 0) {
				return null;
			}
			isNeg = true;
		}
		return [r, m, isNeg ? -1 : r == 0 && m == 0 ? 0 : 1];			
	}
	return v;
}

var toLongI2 = function(i0, i1) {
	var r = i0&0xFFFFFF; // 24
	var l = (i0>>>24) + ((i1&0xFFFF)<<8); // 24
	var h = i1>>>16; // 16
	return fromLongRLH([r,l,h]);
}

var toLongRMS = function(s0, noOver) {
	if (typeof s0 == "number") {
		if (s0 + 1 > s0 && s0 -1 < s0)
			return checkLong([Math.abs(s0), 0, s0 < 0 ? -1 : s0 > 0 ? 1 : 0]);
		
		s0 -= s0%1;
		s0 = "" + s0;
	} else if (Array.isArray(s0)) {
		return s0;
	}
	var isNeg = (s0.indexOf("-") == 0);
	var s = (isNeg ? s0.substring(1) : s0);

	// 1) Split the string into two reasonably-sized numbers
	// give l (low) the last 8 digits; h (high) can have the first 11.
	// max l will be 27 bits; max h will be 37 bits.
	// later we will add in up to 11 more bits into h.

	var pt = s.length - 15;
	var h = Number(pt <= 0 ? 0 : s.substring(0, pt));
	var l = Number(pt <= 0 ? s : s.substring(pt));


	// The task is to partition the number based on binary digits, not decimal.
	// Starting with [l,h] we want [r,m].

	// L = h * T15 + l == m*MAXR + r

	// 2) Split the low-digit part into an m part and an r part
	// using integerization |0 and modulus %.
	// l = lm*MAXR + lr

	var lm = l/MAXR|0;
	var r = l&RMASK;

	// That was the easy part.

	// For the high digits, consider that we must satisfy:
	// h*T15 = hm*MAXR + hr
	// or
	// h*(T15/MAXR) = hm + hr/MAXR
	// notice that T15/MAXR is a decimal with an
	// integer part (ti) and a fractional part (tf):
	// h*(ti + tf)
	// so h*ti = hm and h*tf*MAXR = hr
	// except hr will overflow, so we need to add its high part to hm

	// to avoid integer overflow, we divide both sides by MAXR.
	// bringing in the fractional part of the high number along
	// with the low part of the remainder digits.

	var r0 = h * T15RF; // 0.9604644775390625

	// This is our remainder, except it has almost certainly overflowed.
	// So we need to move its high part from r to m. We cannot
	// use |0 here because this one could be over 31 bits now after
	// adding in h * tf.

	var rh = Math.floor(r0)
	r += Math.floor((r0 - rh)*MAXR);

	lm += r/MAXR|0;
	r &= RMASK;

	// combining the integer high part h * ti with the overflow of the
	// lower numbers (rh and lm):

	var m = h * T15RN + rh + lm;

	// That's i;, we have m, r and s.
	
	if (!r&&!m &&(h||l) || (isNeg ? m >= MSIGNB && r > 0 : m >= MSIGNB)) {
		return (noOver ? null : isNeg ? LONG_MIN_VALUE : LONG_MAX_VALUE);
	} 
	return [r, m, !r&&!m ? 0 : isNeg ? -1 : 1];			
}

var checkLong = function(rms, limit) {
	// returns rms without cloning
	var r = rms[0];
	if (limit > 0 && (r >= -limit && r < limit))
		return rms;
	var m = rms[1];
	if (!rms[2]) {
		if (!r && !m)
			return rms;
		rms[2] = 1;
	}
	if (r < 0) {
		if (m == 0) {
			rms[0] = -r;
			rms[2] = -rms[2];
		} else {
			var rl = r%MAXR;
			var rh = (r - rl) / MAXR;
			var n = m + rh;
			if (n < 1) {
				rms[0] = -rl;
				rms[1] = -n;
				rms[2] = -rms[2];
			} else {
				rms[0] = MAXR + rl
				rms[1] = n - 1;
			}
		}					
	} else if (r >= MAXR) {
		var rl = r%MAXR;
		var rh = (r - rl) / MAXR;
		rms[0] = rl
		rms[1] += rh;
	}
	if (rms[1] >= MSIGNB) {
		if (limit >= 0) {
			return (rms[2] > 0 ? LONG_MAX_VALUE : LONG_MIN_VALUE);
		} 
		while (rms[1] > 0) {
			rms[1] -= MMINMAX;
		}
		if (rms[0] > 0) {
			rms[1]++;
			rms[0] = MAXR - rms[0];
		} 
		rms[1] = -rms[1]; 
		rms[2] = (rms[1] == MSIGNB ? -1 : -rms[2]);	
	} else if (rms[1] <= -MSIGNB) {
		return LONG_MIN_VALUE;
	}
	return rms;
}

Long.$sign = function(a) {
	return (a.length ? a[2] : Math.signum(a));
}

Long.$n = function(a) {
	return BigInt(Long.$s(a));
}

Long.$s = function(a, radix, unsigned) { 
	// todo radix
	radix || (radix = 10);
	if (!a.length) {
		if (radix == 10 && !unsigned)
			return "" + a;
		a = toLongRMS(a);
	}
	if (a[1] == 0 && !unsigned) {
		return (a[2]*a[0]).toString(radix);
	}
	var isNeg = (a[2] == -1);
	checkLong(a);
	var m = a[1];
	var r = a[0];
	var s = "";
	if (unsigned && isNeg && m < MSIGNB) {
		a = toLongRLH(a);
		r = a[0];
		m =a[1]+a[2]*MAXL;
	}
	var zeros = null;
	switch (radix) {
	case 2:  // 24
		zeros = "000000000000000000000000";
		break;
	case 4:  // 12
		zeros = "000000000000";
		break;
	case 8:
		zeros = "00000000"
		break;
	case 16:
		zeros = "000000"
		break;
	case 32:
		isNeg = false;
		m && (s = (m/0x3F).toString(32));
		var s1 = (r + ((m&0x40)<<24)).toString(32);
		s += (s.length == 0 ? s1 : longFill(s1, "00000"));
		break;
	case 10:
		var ml = m % T8;
		var mh = (m - ml)/T8;
		var ll = r + ml * MAXR;
		var l = ll % T8;
		var lh = (ll - l)/T8;
		var h = mh * MAXR + lh;
		var fl = (h == 0 ? "" : "00000000") + ll;
		s = (h != 0 ? h + fl.substring(fl.length - 8) : ll);
		break;
	}
	if (zeros) {
		isNeg = false;
		m && (s = m.toString(radix));
		var s1 = r.toString(radix);
		s += (s.length == 0 ? s1 : longFill(s1, zeros));
	}
	return (!unsigned && isNeg ? "-" : "") + s;
}

var longFill = function(s, zeros) {
	var i,n
	if ((i = s.length) == (n = zeros.length))
		return s;
	s = zeros + s;
	return s.substring(i - n, i);
}

Long.$dup=function(a,b){ 
	if (!a.length) {
		return a;
	}
	if (a[1] == 0) {
		return a[2] * a[0];
	}
	return [a[0],a[1],a[2]]; 
}

Long.$inc = function(x,n) {
	// n +/-1 only;
	var ret;
	if (!x.length) {
		ret = x + n;
		if (ret != x) {
			return ret;
		}
		x = toLongRMS(x);
	}
    return checkLong([x[2] == -1 ? x[0] - n : x[0] + n, x[1], x[2]], -1);
}

Long.$neg=function(a){ return (a.length ? checkLong([a[0],a[1],-a[2]]) : -a); }


Long.$ival=function(a){ return Long.$lval(a)|0; }

Long.$lval=function(a){ return (a.length ? a[2] * (a[0] + (a[1]%MAXR)*MAXR) : a); }

Long.$fval=function(a){ 
	geta32()[0] = (a.length ? a[2] * (a[0] + a[1]*MAXR) : a); 
	return a32[0];
}

Long.$dval=function(a){ 	
	geta64()[0] = (a.length ? a[2] * (a[0] + a[1]*MAXR) : a); 
	return a64[0];
}

var longTest = function(a,b) {
	var isna = (!a.length);
	var isnb = (!b.length);
	return (isna && isnb ? 0 : isna ? -1 : isnb ? 1 : 2);
}

var ab = [0,0];

var fixLongAB = function(a,b) {
	switch (longTest(a,b)) {
	case 0:
		return true;
	case 2:
		checkLong(a); 
		checkLong(b);
		break;
	case 1:
		checkLong(a); 
		b = toLongRMS(b);
		break;
	case -1:
		a = toLongRMS(a);
		checkLong(b);
		break;
	}
	ab[0] = a;
	ab[1] = b;
}


Long.$cmp=function(a,b, unsigned){
	if (fixLongAB(a,b)) {
		return (a < b ? -1 : a > b ? 1 : 0);
	}
	a = ab[0];b = ab[1];
	if (unsigned) {
		a = toLongRLH(a);
		b = toLongRLH(b);
		for (let i = 2; i >= 0; i--) {
			if (a[i] < b[i]) return -1;
			if (a[i] > b[i]) return 1;
		}
		return 0;
	}
	return (
		a[2] < b[2] ? -1
		: a[2] > b[2] ? 1
		: a[2] == 0 ? 0
		: a[1] < b[1] ? -a[2]
		: a[1] > b[1] ? a[2]
		: a[0] < b[0] ? -a[2]
		: a[0] > b[0] ? a[2]
		: 0
	);
}

Long.$eq=function(a,b){
	return Long.$cmp(a, b) == 0;
}

Long.$ne=function(a,b){
	return Long.$cmp(a, b) != 0;
}

Long.$ge=function(a,b){
	return Long.$cmp(a, b) >= 0;
}

Long.$gt=function(a,b){
	return Long.$cmp(a, b) > 0;
}

Long.$le=function(a,b){
	return Long.$cmp(a, b) <= 0;
}

Long.$lt=function(a,b){
	return Long.$cmp(a, b) < 0;
}


Long.$not=function(a){
	return (a.length ? checkLong([a[0]+a[2],a[1],-a[2]]) : -a - 1);
}

var m2 = [0,1,3,7
	,0xF,0x1F,0x3F,0x7F
	,0xFF,0x1FF,0x3FF,0x7FF
	,0xFFF,0x1FFF,0x3FFF,0x7FFF
	,0xFFFF,0x1FFFF,0x3FFFF,0x7FFFF
	,0xFFFFF,0x1FFFFF,0x3FFFFF,0x7FFFFF
	,0xFFFFFF
	];

Long.$sr=function(a,n){
	if (arguments.length > 2)
		return doLong(Long.$sr,arguments);
	return shiftLong(a, n, 0);
}

Long.$usr=function(a,n){
	if (arguments.length > 2)
		return doLong(Long.$usr,arguments);
	return shiftLong(a, n, 1);
}

var shiftLong = function(a, n, unsigned) {
	if (fixLongAB(a,n)) {
		if (a >= 0 && a == (a|0)) 
			return (n > 31 ? 0 : a>>n);
		if (n >= 64) 
			return (a > 0 || unsigned ? 0 : -1);
		if (n == 0)
			return a;
		a = toLongRMS(a);
	} else {
		a = ab[0];
		n = (ab[1][1] > 0 ? 64 : ab[1][0]);
	}
	if (n >= 64 || a[2] == 0) {
		return (a[2] >= 0 || unsigned ? 0 : -1);
	} else if (n == 0) {
		return Long.$dup(a);
	}
	var isNeg = (a[2] < 0);
	var a = toLongRLH(a);
	var c2,c1;
	if (isNeg && !unsigned)
		a[2] = -1&~HMASK|a[2];
	if (n >= 48) {
		a[0] = a[1] = 0;
		a[2] && (a[0] = a[2]>>(n-48),a[2] = 0);
		isNeg && !unsigned && a[0] && (a[1]=-1,a[2] = -1);
		return fromLongRLH(a);	
	} 
	if (n >= 24) {
		a[0] = a[1];
		a[1] = a[2];
		a[2] = 0;
		n -= 24;
	}
	
	a[2] && (c2=(a[2]&m2[n])<<(24-n),a[2]>>=n);
	a[1] && (c1=(a[1]&m2[n])<<(24-n),a[1]>>=n);
	c2 && (a[1] += c2);
	a[0] && (a[0]>>=n);
	c1 && (a[0] += c1);
	return fromLongRLH(a);	
}

Long.$sl = function(a, n){
	if (arguments.length > 2)
		return doLong(Long.$sl,arguments);
	if (fixLongAB(a,n)) {
		var c;
		if (a == 0 || n == 0)
			return a;
		if (n < 32 && (a < 0 ? (c = a<<n) < a : (c = a<<n) > a))
			return c;
		if (n >= 64) 
			return 0;
		a = toLongRMS(a);
	} else {
		a = ab[0];
		n = (ab[1][1] > 0 ? 64 : ab[1][0]);
	}
	if (n >= 64 || a[2] == 0) {
		return 0;
	} 
	if (n == 0) {
		return Long.$dup(a);
	}
	var a = toLongRLH(a);
	var c2,c1;
	if (n < 24) {
		a[2] && (a[2]<<=n);
		a[1] && (a[2]|=a[1]>>(24-n),a[1]<<=n);
		a[0] && (a[1]|=a[0]>>(24-n),a[0]<<=n,a[0]&=RMASK);		
	} else if (n < 48) {
		a[2] = (a[0]?a[0]>>(48-n):0);
		a[1] && (a[2]|=a[1]<<(n-24));
		a[0] && (a[1]=a[0]<<(n-24),a[0]=0);
	} else {
		a[1] = 0;
		a[2] = (a[0]?a[0]<<(n-48):0);
		a[0] = 0;
	} 
	a[1] && (a[1]&=LMASK);
	a[2] && (a[2]&=HMASK);
	return fromLongRLH(a);	
}

var addAB = function(a,b, s) {
	if (fixLongAB(a,b)) {
		var r = a + s * b;
		if (r + 1 > r && r - 1 < r)
			return r;
		ab[0] = toLongRMS(a);
		ab[1] = toLongRMS(b);		
	}
	a = ab[0];b = ab[1];
	var r,m;
	if (a[2] < 0)
		s = -s;
	r = a[0] + s * b[0] * b[2];
	m = a[1] + s * b[1] * b[2];
	if (m == 0)
		return (r == 0 ? 0 : r * (a[2] || 1));
	return checkLong([r,m,a[2]], -1);
}

Long.$add = function(a,b) {
	if (arguments.length > 2)
		return doLong(Long.$add,arguments);
	return addAB(a,b, 1);
}

Long.$sub = function(a,b) {
	if (arguments.length > 2)
		return doLong(Long.$sub,arguments);
	return addAB(a,b, -1);
}

Long.$and=function(a,b){
	if (arguments.length > 2)
		return doLong(Long.$and,arguments);
	if (fixLongAB(a,b)) {
		if (a == 0 || b == 0)
			return 0;
		if ((a|0) == a && (b|0) == b)
			return a&b;
		ab[0] = toLongRMS(a);
		ab[1] = toLongRMS(b);		
	}
	a = toLongRLH(ab[0]);b = toLongRLH(ab[1]);
	a[0] = a[0]&b[0];
	a[1] = a[1]&b[1];
	a[2] = a[2]&b[2];
	return fromLongRLH(a);	
}

Long.$or=function(a,b){ 
	if (arguments.length > 2)
		return doLong(Long.$or,arguments);
	if (fixLongAB(a,b)) {
		if (a == 0 && b == 0)
			return 0;
		if ((a|0) == a && (b|0) == b)
			return a|b;
		ab[0] = toLongRMS(a);
		ab[1] = toLongRMS(b);		
	}
	a = toLongRLH(ab[0]);b = toLongRLH(ab[1]);
	a[0] = a[0]|b[0];
	a[1] = a[1]|b[1];
	a[2] = a[2]|b[2];
	return fromLongRLH(a);	
}

Long.$xor=function(a,b){
	if (arguments.length > 2)
		return doLong(Long.$xor,arguments);
	if (fixLongAB(a,b)) {
		if (a == 0 && b == 0)
			return 0;
		if ((a|0) == a && (b|0) == b)
			return a^b;
		ab[0] = toLongRMS(a);
		ab[1] = toLongRMS(b);		
	}
	a = toLongRLH(ab[0]);b = toLongRLH(ab[1]);
	a[0] = a[0]^b[0];
	a[1] = a[1]^b[1];
	a[2] = a[2]^b[2];
	return fromLongRLH(a);	
}

Long.$mul = function(a,b){ 
	if (arguments.length > 2)
		return doLong(Long.$mul,arguments);
	if (fixLongAB(a,b)) {
		var r = a*b; 
		if (r + 1 > r && r - 1 < r)
			return r;
		ab[0] = toLongRMS(a);
		ab[1] = toLongRMS(b);		
	}
	var a = ab[0];
	var b = ab[1];
	if (a[2] == 0 || b[2] == 0)
		return 0;
	var isNeg = (a[2]*b[2] < 0);
	if (a[2] < 0)
		a = [a[0], a[1], 1];
	if (b[2] < 0)
		b = [b[0], b[1], 1];
	a = toLongRLH(a);
	b = toLongRLH(b);
	var r = a[0]*b[0];
	var c = (r-r%MAXR)/MAXR;
	var l = a[0]*b[1]+a[1]*b[0]+c;
	c = (l-l%MAXL)/MAXL;
	var h = a[0]*b[2]+a[2]*b[0]+a[1]*b[1]+c;
	a = fromLongRLH([r,l,h]);
	return (isNeg ? Long.$neg(a) : a);
}

Long.$div=function(a,b){
	if (arguments.length > 2)
		return doLong(Long.$div,arguments);
	if (fixLongAB(a,b)) {
		if (b == 0)
	    	arex("/ by zero");
		return (a/b) - (a/b)%1;
	}
	var a = ab[0];
	var b = ab[1];
	if (b[2] == 0)
    	arex("/ by zero");
	if (a[2] == 0 || b[1] > a[1] || b[1] == a[1] && b[0] > a[0])
		return 0;
	var isNeg = (a[2]*b[2] < 0);
	if (a[2] < 0)
		a = [a[0], a[1], 1];
	if (b[2] < 0)
		b = [b[0], b[1], 1];
	if (b[1] == a[1]) {
		// the result will only depend upon r, not m
		return  (isNeg ? -1 : 1)*(a[0]/b[0])|0;
	}
	var d = NaN;
	if (b[1] == 0) {
		d = b[0];
	} else {
		d = b[0] + b[1] * MAXR;
		if (d + 1 == d || d - 1 == d) {
			d = NaN;
		}
	}
	if (isNaN(d)) {
		// only from very large numbers divided by very large numbers.
		// never in BigInteger, as that is all long/int
		var bi = self.BigInt; 
		var f = (bi || Long.toUnsignedBigInteger$J);
		var m = f(MAXR);
		var aBr = f(a[0]);
		var aBm = f(a[1]);
		var bBr = f(b[0]);
		var bBm = f(b[1]);
		if (bi) {
			ret = Number((aBr + m * aBm)/(bBr + m * bBm));
		} else {
			// SwingJS aliases in BigInteger
			ret = aBr.add(m.mul(aBm)).div(bBr.add(m.mul(bBm)));
		}
		return Clazz.toLong((isNeg ? -1 : 1) * ret);
	}
	var m = a[1]/d;
	var mf = m%1;
	var r = a[0]/d + mf*MAXR;
	var rf = r%1;
	m -= mf;
	r -= rf;
	// rounding could be no more than +/-1 off
	switch (Long.$cmp(a,Long.$mul([r, m, 1],d))) {
	case 0:
		break;
	case -1:// a < b*d -- too high
		r -= 1;
		break;
	case 1:
		if (Long.$cmp(a,Long.$mul([r+1, m, 1],d)) >= 0) {
			r += 1;
		}
		break;
	}
	return checkLong([r, m, isNeg ? -1 : 1]);
}

Long.$mod=function(a,n){
	if (arguments.length > 2)
		return doLong(Long.$mod,arguments);
	if (fixLongAB(a,n)) {
		return a%n;
	}
	// a mod n = a - (a/n)*n
	return Long.sub(a,Long.mul(Long.div(a,n),n));
}

var doLong = function(f,args) {
	var a = args[0];
	for (var i = 1; i < args.length; i++) {
		a = f(a, args[i]);
	}
	return a;
}

var toLongRLH = function(rms) {
	// to [16][24][24]
	// h l r
	var r = rms[0];
	var m = rms[1];
	if (m == 0 && r == 0) {
		return [0,0,0];
	}
	var isNeg = (rms[2] == -1);
	var ml = m&LMASK;
	m = (m - ml)/MAXL;
	if (isNeg) {
		r = (~r&RMASK) + 1;
		ml = (~ml&LMASK) + (r == MAXR ? 1 : 0);   
		m = (~m&HMASK) + (ml == MAXL ? 1 : 0); 
		r &= RMASK;
		ml &= LMASK;
		m &= HMASK;
	}
	return [r,ml,m];
}

var fromLongRLH = function(rlh) {
	var isNeg = (rlh[1] < 0);
	(isNeg ? (rlh[2] = -1) : (isNeg = rlh[2] < 0 || rlh[2] >= HSIGNB)); 
	r = rlh[0]&RMASK; 
	m = ((rlh[2]&HMASK)*MAXL)+(rlh[1]&LMASK);
	if (m == 0 && r == 0) {
		return 0;
	}
	if (isNeg) {   
		r = (~r&RMASK) + 1;
		m = MAXM - m - (r == MAXR ? 0 : 1);   
		r &= RMASK;
	}
	return checkLong([r,m, !r&&!m ? 0 : isNeg ? -1 : 1]);
}

// Long.TYPE=Long.prototype.TYPE=Long;
// Note that the largest usable "Long" in JavaScript is 53 digits:

Long.MIN_VALUE=Long.prototype.MIN_VALUE=LONG_MIN_VALUE;
Long.MAX_VALUE=Long.prototype.MAX_VALUE=LONG_MAX_VALUE;

var maxLong =  0x1000000000000000000000; // ignored
var minLong = -maxLong;
Long.SIZE=Long.prototype.SIZE=64;// REALLY 53


m$(Long,["intValue","intValue$"],function(){return Long.$ival(this.valueOf());});

m$(Long,["longValue","longValue$"],function(){return this.valueOf();});

m$(Long,"c$",
function(v) {
	if (typeof v != "number" && typeof v != "object")
		v = Long.parseLong$S$I(v, 10);
 this.valueOf=function(){return v;};
}, 1);

m$(Long, "c$$J", function(v){
	this.valueOf=function(){return v;};
}, 1);

m$(Long,"c$$S",
function(v){
 var v = Long.parseLong$S$I(v, 10);
 this.valueOf=function(){return v;}; 
}, 1);

Long.compare$J$J = function(a,b) { return Long.$cmp(a,b); }

m$(Long,"compareTo$Long", function(l){return Long.$cmp(this.valueOf(), l.valueOf());});

m$(Long,"valueOf$S",
function(s){
	return Long.valueOf$S$I(s, 10);
}, 1);

m$(Long,"valueOf$S$I",
function(s, radix){
  return Long.valueOf$J(Long.parseLong$S$I(s, radix));
}, 1);

m$(Long,"valueOf$J",
function(i){
  i = Clazz.toLong(i);
  var v = (!i.length || (v = Long.$ival(i)) == Long.$lval(i) && (i = v) == i ? getCachedNumber(i, longs, Long, "c$$J") : 0);
  return (v ? v : Clazz.new_(Long.c$$J, [i]));
}, 1);

m$(Long,"hashCode$",function(){return Long.$ival(this.valueOf());});
m$(Long,"doubleValue$",function(){return Long.$dval(this.valueOf());});
m$(Long,"floatValue$",function(){return Long.$fval(this.valueOf());});


m$(Long,"parseLong$S",
function(s){
	return Long.parseLong$S$I(s,10);
}, 1);

m$(Long,"parseLong$S$I",
function(s,radix){
	var v,v0;
	if (s.indexOf("x") == 1 || isNaN(v0 = parseInt(s, radix)) || (v = parseLong(s, radix, v)) == null)
		throw Clazz.new_(NumberFormatException.c$$S, ["parsing " + s + " radix " + radix]);
	return v;
}, 1);

m$(Long,"decode$S",
function(n){
    if (n.length() == 0)
        throw new NumberFormatException("Zero length string");
	
	if (n.indexOf(".") >= 0)n = "";
	var isNeg = (n.startsWith(i));
	var i = (isNeg ? 1 : 0);
	n = n.replace(/\#/, "0x").toLowerCase();
	var radix = 10;
	if (n.startsWith("0x", i)) {
		radix = 16;
		i += 2;
	} else if (n.startsWith("0", i)) {
		i += 1;
		radix = 8;
	}
	var result;
    try {
        result = Long.valueOf$S$I(n.substring$I(i), radix);
        result = (isNeg ? Long.valueOf$J(-result.longValue$()) : result);
    } catch (e) {
        var constant = (isNeg ? "-" + n.substring(i) : n.substring$I(i));
        result = Long.valueOf$S$I(constant, radix);
    }
    return result;
}, 1);

m$(Long,"equals$O",
		function(s){
		return (s instanceof Long) && Long.$eq(s.valueOf(),this.valueOf());
});

m$(Long, "$box$", function(v) {
	return Long.valueOf$J(v.longValue$());
});

;(function(C$) {

m$(C$, 'compareUnsigned$J$J', function (x, y) {
return C$.compare$J$J(Long.$add(x,LONG_MIN_VALUE), Long.$add(y,LONG_MIN_VALUE));
}, 1);

// Long.compareUnsigned$J$J = function(a,b) { return Long.$cmp(a,b,1); }

m$(C$, 'divideUnsigned$J$J', function (dividend, divisor) {
if (Long.$lt(divisor,0 )) {
return (C$.compareUnsigned$J$J(dividend, divisor)) < 0 ? 0 : 1;
}if (Long.$gt(dividend,0 )) return Long.$div(dividend,divisor);
 else {
return C$.toUnsignedBigInteger$J(dividend).divide$java_math_BigInteger(C$.toUnsignedBigInteger$J(divisor)).longValue$();
}}, 1);

m$(C$, 'remainderUnsigned$J$J', function (dividend, divisor) {
if (Long.$gt(dividend,0 ) && Long.$gt(divisor,0 ) ) {
return Long.$mod(dividend,divisor);
} else {
if (C$.compareUnsigned$J$J(dividend, divisor) < 0) return dividend;
 else return C$.toUnsignedBigInteger$J(dividend).remainder$java_math_BigInteger(C$.toUnsignedBigInteger$J(divisor)).longValue$();
}}, 1);

m$(C$, 'highestOneBit$J', function (i) {
(i=Long.$or(i,((Long.$sr(i,1)))));
(i=Long.$or(i,((Long.$sr(i,2)))));
(i=Long.$or(i,((Long.$sr(i,4)))));
(i=Long.$or(i,((Long.$sr(i,8)))));
(i=Long.$or(i,((Long.$sr(i,16)))));
(i=Long.$or(i,((Long.$sr(i,32)))));
return Long.$sub(i,(Long.$usr(i,1)));
}, 1);

m$(C$, 'lowestOneBit$J', function (i) {
return Long.$and(i,(Long.$neg(i)));
}, 1);

m$(C$, 'numberOfLeadingZeros$J', function (i) {
if (Long.$eq(i,0 )) return 64;
var n=1;
var x=Long.$ival((Long.$usr(i,32)));
if (x == 0) {
n+=32;
x=Long.$ival(i);
}if (x >>> 16 == 0) {
n+=16;
x<<=16;
}if (x >>> 24 == 0) {
n+=8;
x<<=8;
}if (x >>> 28 == 0) {
n+=4;
x<<=4;
}if (x >>> 30 == 0) {
n+=2;
x<<=2;
}n-=x >>> 31;
return n;
}, 1);

m$(C$, 'numberOfTrailingZeros$J', function (i) {
var x;
var y;
if (Long.$eq(i,0 )) return 64;
var n=63;
y=Long.$ival(i);
if (y != 0) {
n=n - 32;
x=y;
} else x=Long.$ival((Long.$usr(i,32)));
y=x << 16;
if (y != 0) {
n=n - 16;
x=y;
}y=x << 8;
if (y != 0) {
n=n - 8;
x=y;
}y=x << 4;
if (y != 0) {
n=n - 4;
x=y;
}y=x << 2;
if (y != 0) {
n=n - 2;
x=y;
}return n - ((x << 1) >>> 31);
}, 1);

m$(C$, 'bitCount$J', function (i) {
i=Long.$sub(i,(Long.$and((Long.$usr(i,1)),[5592405,366503875925,1])));
i=Long.$add((Long.$and(i,[3355443,219902325555,1])),(Long.$and((Long.$usr(i,2)),[3355443,219902325555,1])));
i=Long.$and((Long.$add(i,(Long.$usr(i,4)))),[986895,64677154575,1]);
i=Long.$add(i,(Long.$usr(i,8)));
i=Long.$add(i,(Long.$usr(i,16)));
i=Long.$add(i,(Long.$usr(i,32)));
return Long.$ival(i) & 127;
}, 1);

m$(C$, 'rotateLeft$J$I', function (i, distance) {
return Long.$or((Long.$sl(i,distance)),(Long.$usr(i,-distance)));
}, 1);

m$(C$, 'rotateRight$J$I', function (i, distance) {
return Long.$or((Long.$usr(i,distance)),(Long.$sl(i,-distance)));
}, 1);

m$(C$, 'reverse$J', function (i) {
i=Long.$or(Long.$sl((Long.$and(i,[5592405,366503875925,1])),1),Long.$and((Long.$usr(i,1)),[5592405,366503875925,1]));
i=Long.$or(Long.$sl((Long.$and(i,[3355443,219902325555,1])),2),Long.$and((Long.$usr(i,2)),[3355443,219902325555,1]));
i=Long.$or(Long.$sl((Long.$and(i,[986895,64677154575,1])),4),Long.$and((Long.$usr(i,4)),[986895,64677154575,1]));
i=Long.$or(Long.$sl((Long.$and(i,[16711935,4278255360,1])),8),Long.$and((Long.$usr(i,8)),[16711935,4278255360,1]));
i=Long.$or((Long.$sl(i,48)),(Long.$sl((Long.$and(i,4294901760)),16)) , (Long.$and((Long.$usr(i,16)),4294901760)) , (Long.$usr(i,48)) );
return i;
}, 1);

m$(C$, 'signum$J', function (i) {
return Long.$sign(i);
//Long.$ival((Long.$or((Long.$sr(i,63)),(Long.$usr((Long.$neg(i)),63)))));
}, 1);

m$(C$, 'reverseBytes$J', function (i) {
i=Long.$or(Long.$sl((Long.$and(i,[16711935,4278255360,1])),8),Long.$and((Long.$usr(i,8)),[16711935,4278255360,1]));
return Long.$or((Long.$sl(i,48)),(Long.$sl((Long.$and(i,4294901760)),16)) , (Long.$and((Long.$usr(i,16)),4294901760)) , (Long.$usr(i,48)) );
}, 1);

m$(C$, 'sum$J$J', function (a, b) {
return Long.$add(a,b);
}, 1);

m$(C$, 'max$J$J', function (a, b) {
return (Long.$ge(a,b) ? a : b);
}, 1);

m$(C$, 'min$J$J', function (a, b) {
return (Long.$le(a,b) ? a : b);
}, 1);

Clazz.newMeth(C$, 'getLong$S', function (nm) {
	return C$.getLong$S$Long(nm, null);
}, 1);

Clazz.newMeth(C$, 'getLong$S$J', function (nm, val) {
	var result=C$.getLong$S$Long(nm, null);
	return (Long.$eq(result,null )) ? C$.valueOf$J(val) : result;
}, 1);

Clazz.newMeth(C$, 'getLong$S$Long', function (nm, val) {
	var v=null;
	try {
	v=System.getProperty$S(nm);
	} catch (e) {
	if (Clazz.exceptionOf(e,"IllegalArgumentException") || Clazz.exceptionOf(e,"NullPointerException")){
	} else {
	throw e;
	}
	}
	if (v != null ) {
	try {
	return C$.decode$S(v);
	} catch (e) {
	if (Clazz.exceptionOf(e,"NumberFormatException")){
	} else {
	throw e;
	}
	}
	}return val;
}, 1);

Clazz.newMeth(C$, 'parseUnsignedLong$S', function (s) {
	return C$.parseUnsignedLong$S$I(s, 10);
}, 1);

Clazz.newMeth(C$, 'parseUnsignedLong$S$I', function (s, radix) {
	if (s == null ) {
	throw Clazz.new_(Clazz.load('NumberFormatException').c$$S,["null"]);
	}var len=s.length$();
	if (len > 0) {
	var firstChar=s.charAt$I(0);
	if (firstChar == "-") {
	throw Clazz.new_(Clazz.load('NumberFormatException').c$$S,[String.format$S$OA("Illegal leading minus sign on unsigned string %s.", Clazz.array(java.lang.Object, -1, [s]))]);
	} else {
	if (len <= 12 || (radix == 10 && len <= 18 ) ) {
	return C$.parseLong$S$I(s, radix);
	}var first=C$.parseLong$S$I(s.substring$I$I(0, len - 1), radix);
	var second=Character.digit$C$I(s.charAt$I(len - 1), radix);
	if (second < 0) {
	throw Clazz.new_(Clazz.load('NumberFormatException').c$$S,["Bad digit at end of " + s]);
	}var result=Long.$add(Long.$mul(first,radix),second);
	if (C$.compareUnsigned$J$J(result, first) < 0) {
	throw Clazz.new_(Clazz.load('NumberFormatException').c$$S,[String.format$S$OA("String value %s exceeds range of unsigned long.", Clazz.array(java.lang.Object, -1, [s]))]);
	}return result;
	}} else {
	throw Clazz.load('NumberFormatException').forInputString$S(s);
}}, 1);


})(Long);
		
Long.toUnsignedString$J=Long.toUnsignedString$J$I = function(i,r) {
	if (i >= 0)
		return Long.toString$J$I(i,r || 10);
    switch (r || 10) {
    case 2:
        return Long.toBinaryString$J(i);
    case 4:
    	return Long.$s(j,4,1)
    case 8:
        return Long.toOctalString$J(i);
    case 16:
        return Long.toHexString$J(i);
    case 32:
    default:
        return Long.$s(i,r,1);
    }
};
Long.sum$J$J = Long.$add;
Long.toHexString$J=function(j) { return Long.$s(j,16,1)};
Long.toOctalString$J=function(j) { return Long.$s(j,8,1)};
Long.toBinaryString$J=function(j) { return Long.$s(j,2,1)};

var bi;
Long.toUnsignedBigInteger$J = function(i) {
    bi || (bi=(Clazz.load("java.math.BigInteger"), Clazz.new_(java.math.BigInteger.c$$S,["18446744073709551616"])));
    return (i >= 0 ? bi.valueOf$J(i) : bi.valueOf$J(i).add$java_math_BigInteger(bi));
}
    
Clazz._setDeclared("java.lang.Short", java.lang.Short = Short = function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
});
decorateAsNumber(Short, "Short", "short", "H", sHCOffset);


var minShort = Short.MIN_VALUE = Short.prototype.MIN_VALUE = -32768;
var maxShort = Short.MAX_VALUE = Short.prototype.MAX_VALUE = 32767;
Short.SIZE=Short.prototype.SIZE=16;

m$(Short,"c$", function(v){ // SwingJS only -- for new Integer(3)
	v || v == null || (v = 0);
	if (typeof v != "number")
		v = Short.parseShort$S$I(v, 10);
	this.valueOf=function(){return v;};
	}, 1);


m$(Short, "c$$H", function(v){
	this.valueOf=function(){return v;};
}, 1);


m$(Short,"c$$S",
function(v){
 var v = Short.parseShort$S$I(v, 10);
 this.valueOf=function(){return v;}; 
}, 1);


m$(Short,"valueOf$S",
function(s){
	return Short.valueOf$S$I(s, 10);
}, 1);

m$(Short,"valueOf$S$I",
function(s, radix){
  return Short.valueOf$H(Short.parseShort$S$I(s, radix));
}, 1);

m$(Short,"valueOf$H",
function(i){
  var v = getCachedNumber(i, shorts, Short, "c$$H");
  return (v ? v : Clazz.new_(Short.c$$H, [i]));
}, 1);


m$(Short,"parseShort$S",
function(s){
	return parseIntLimit(s, false, minShort, maxShort);
}, 1);

m$(Short,"parseShort$S$I",
function(s,radix){
	return parseIntLimit(s, radix, minShort, maxShort);
}, 1);


Short.toString = Short.toString$H = Short.toString$H$I = Short.prototype.toString = function (i,radix) {
	switch(arguments.length) {
	case 2:
		return i.toString(radix);
	case 1:
		return "" +i;
	case 0:
		return (this===Short ? "class java.lang.Short" : ""+this.valueOf());
	}
};


Short.toUnsignedInt$H = Short.toUnsignedLong$H = function (i) {
  return (i < 0 ? i + 0x10000 : i);
};

m$(Short, "decode$S",
function(n){
  if (isNaN(n = Integer.decodeRaw$S(n)) || n < -32768|| n > 32767)
    throw Clazz.new_(NumberFormatException.c$$S, ["Invalid Short"]);
  return Clazz.new_(Short.c$$H, [n]);
}, 1);


m$(Short, "equals$O", function(s){
	return (s instanceof Short) && s.valueOf()==this.valueOf();
});

m$(Short, "$box$", function(v) {
	return Short.valueOf$H(v.shortValue$());
});


Clazz._setDeclared("Byte", java.lang.Byte=Byte=function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
});
decorateAsNumber(Byte,"Byte", "byte", "B", bHCOffset);

// Byte.serialVersionUID=Byte.prototype.serialVersionUID=-7183698231559129828;
var minByte = Byte.MIN_VALUE=Byte.prototype.MIN_VALUE=-128;
var maxByte = Byte.MAX_VALUE=Byte.prototype.MAX_VALUE=127;
Byte.SIZE=Byte.prototype.SIZE=8;

m$(Byte,"c$", function(v){ // SwingJS only -- for new Integer(3)
	v || v == null || (v = 0);
	if (typeof v != "number")
		v = Byte.parseByte$S$I(v, 10);
	this.valueOf=function(){return v;};
	}, 1);


m$(Byte, "c$$B", function(v){
	this.valueOf=function(){return v;};
}, 1);

m$(Byte,"c$$S",
function(v){
 var v = Byte.parseByte$S$I(v, 10);
 this.valueOf=function(){return v;}; 
}, 1);


m$(Byte,"valueOf$S",
function(s){
	return Byte.valueOf$S$I(s, 10);
}, 1);

m$(Byte,"valueOf$S$I",
function(s, radix){
  return Byte.valueOf$B(Byte.parseByte$S$I(s, radix));
}, 1);

m$(Byte,"valueOf$B",
function(i){
  var v = getCachedNumber(i, bytes, Byte, "c$$B");
  return v;
}, 1);


Byte.toString=Byte.toString$B=Byte.toString$B$I=Byte.prototype.toString=function(i,radix){
	switch(arguments.length) {
	case 2:
		return i.toString(radix);
	case 1:
		return "" +i;
	case 0:
		return (this===Byte ? "class java.lang.Byte" : ""+this.valueOf());
	}
};

m$(Byte,"parseByte$S",
	function(s){
		return parseIntLimit(s, false, minByte, maxByte);
	}, 1);

m$(Byte,"parseByte$S$I",
	function(s,radix){
		return parseIntLimit(s, radix, minByte, maxByte);
	}, 1);

Byte.toString=Byte.toString$B=Byte.toString$B$I=Byte.prototype.toString=function(i,radix){
	switch(arguments.length) {
	case 2:
		return i.toString(radix);
	case 1:
		return "" +i;
	case 0:
		return (this===Byte ? "class java.lang.Byte" : ""+this.valueOf());
	}
};


m$(Byte, ["valueOf$S","valueOf$B","valueOf$S$I"],
function (s,radix) {
  return Clazz.new_(Byte.c$, [s, radix||10]);
}, 1);

m$(Byte,"equals$O",
function(s){
return (s instanceof Byte) && s.valueOf()==this.valueOf();
});

m$(Byte, "$box$", function(v) {
	return Byte.valueOf$B(v.byteValue$());
});

Byte.toUnsignedInt$B = Byte.toUnsignedLong$B = function (i) {
	return (i < 0 ? i + 0x100 : i);
};

m$(Byte,"decode$S",
function(n){
  if (isNaN(n = Integer.decodeRaw$S(n)) || n < -128|| n > 127)
    throw Clazz.new_(NumberFormatException.c$$S, ["Invalid Byte"]);
  return Clazz.new_(Byte.c$$B, [n]);
}, 1);

Clazz._floatToString = function(f) {
	if (f === 0) {
		return (1/f == -Infinity ? "-0.0" : "0.0");
	}
 var check57 = (Math.abs(f) >= 1e-6 && Math.abs(f) < 1e-3);
 if (check57)
	f/=1e7;
 var s = (""+f).replace('e','E');
 if (s.indexOf(".") < 0 && s.indexOf("Inf") < 0 && s.indexOf("NaN") < 0) {
   if(s.indexOf('E') < 0)
	s += ".0"; 
   else {
	s = s.replace('E', '.0E');
   }
 } 
 if (check57) {
	s = s.substring(0, s.length - 2) + (parseInt(s.substring(s.length - 2)) - 7);
	s = s.replace(".0000000000000001",".0");
 }
 return s;
}

Clazz._setDeclared("Float", java.lang.Float=Float=function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
});
decorateAsNumber(Float,"Float", "float", "F");

var maxFloat = 3.4028235E38;
var minFloat = -3.4028235E38;

m$(Float,"c$", function(v){
	v || v == null || v != v || (v == 0) || (v = 0);
	if (typeof v != "number") 
	v = Float.parseFloat$S(v);
	this.valueOf=function(){return v;}
	}, 1);

m$(Float, "c$$F", function(v){
	this.valueOf=function(){return v;};
}, 1);

m$(Float, "c$$S", function(v){
  v = Float.parseFloat$S(v);
 this.valueOf=function(){return v;}
}, 1);

m$(Float, "c$$D", function(v){
	  v || v != v || (v == 0) || (v = 0);
  v = (v < minFloat ? -Infinity : v > maxFloat ? Infinity : v);
 this.valueOf=function(){return v;}
}, 1);

Float.toString=Float.toString$F=Float.prototype.toString=function(){
if(arguments.length!=0){
return Clazz._floatToString(arguments[0]);
}else if(this===Float){
return"class java.lang.Float";
}
return Clazz._floatToString(this.valueOf());
};

var a32, i32, a64, i64;

var geti32 = function() {
	return i32 || (a32 = new Float32Array(1), i32 = new Int32Array(a32.buffer));
}
var geta32 = function() {
	geti32();
	return a32;
}

geti64 = function() {
	return i64 || (a64 = new Float64Array(1), i64 = new Uint32Array(a64.buffer));
}
geta64 = function() {
	geti64();
	return a64;
}

Float.floatToIntBits$F = function(f) {
	return Float.floatToRawIntBits$F(f);
}

Float.floatToRawIntBits$F = function(f) {
	geta32()[0] = f;
	return i32[0]; 
}

Float.intBitsToFloat$I = function(i) {
	geti32()[0] = i;
	return a32[0]; 
}

Float.serialVersionUID=Float.prototype.serialVersionUID=-2671257302660747028;
Float.MIN_VALUE=Float.prototype.MIN_VALUE=1.4e-45;
Float.MAX_VALUE=Float.prototype.MAX_VALUE=3.4028235e+38;
Float.NEGATIVE_INFINITY=Float.prototype.NEGATIVE_INFINITY = Number.NEGATIVE_INFINITY;
Float.POSITIVE_INFINITY=Float.prototype.POSITIVE_INFINITY = Number.POSITIVE_INFINITY;
Float.NaN=Number.NaN;

m$(Float,"parseFloat$S",
function(s){
	var v = Double.parseDouble$S(s);
	return (v < minFloat ? -Infinity : v > maxFloat ? Infinity : v);
}, 1);

m$(Float,"valueOf$S",
function(s){
return Clazz.new_(Float.c$$S, [s]);
}, 1);


m$(Float,["longValue$","longValue"],function(){return Math.floor(this.valueOf());});

m$(Float,"valueOf$D",
function(i){
return Clazz.new_(Float.c$$F, [i < minFloat ? -Infinity : i > maxFloat ? Infinity : i]);
}, 1);

m$(Float,"valueOf$F",
function(i){
return Clazz.new_(Float.c$$F, [i]);
}, 1);

m$(Float,"isNaN$F",
function(num){
return isNaN(num);
}, 1);

m$(Float,"isNaN$",
function(){
return isNaN(this.valueOf());
});

m$(Float,"isInfinite$F",
function(num){
return num == num && !Number.isFinite(num);
}, 1);

m$(Float,"isInfinite$",
function(){
	var v = this.valueOf();
return v == v && !Number.isFinite(this.valueOf());
});

m$(Float,"equals$O",
function(s){
return (s instanceof Float) && s.valueOf()==this.valueOf();
});

m$(Float, "$box$", function(v) {
	return Float.valueOf$F(v.floatValue$());
});

Clazz._setDeclared("Double", java.lang.Double=Double=function(){
  if (typeof arguments[0] == "number") {
	  this.c$$D(arguments[0]);
  } else if (arguments[0] === null || typeof arguments[0] != "object") {
	  this.c$(arguments[0]);
  }
});
decorateAsNumber(Double,"Double", "double", "D");

Double.serialVersionUID=Double.prototype.serialVersionUID=-9172774392245257468;
Double.MIN_VALUE=Double.prototype.MIN_VALUE=4.9e-324;
Double.MAX_VALUE=Double.prototype.MAX_VALUE=1.7976931348623157e+308;
Double.NEGATIVE_INFINITY=Number.NEGATIVE_INFINITY;
Double.POSITIVE_INFINITY=Number.POSITIVE_INFINITY;
Double.NaN=Number.NaN;
// Double.TYPE=Double.prototype.TYPE=Double;

Double.toString=Double.toString$D=Double.prototype.toString=function(){
if(arguments.length!=0){
return Clazz._floatToString(arguments[0]);
}else if(this===Double){
return"class java.lang.Double";
}
return Clazz._floatToString(this.valueOf());
};

m$(Double, "c$$D", function(v){
    v || v != v || (v == 0) || (v = 0);
	this.valueOf=function(){return v;};
}, 1);

m$(Double,"c$", function(v){
	// -0 here becomes 0, from Double.valueOf(d)
  v || v == null || v != v || (v = 0);
 if (typeof v != "number") 
  v = Double.parseDouble$S(v);
 this.valueOf=function(){return v;}
}, 1);

m$(Double, ["c$$S"], function(v){
v || v == null || (v == 0) || (v = 0);
if (typeof v != "number") 
	v = Double.parseDouble$S(v);
this.valueOf=function(){return v;};
}, 1);

Double.prototype.isNaN$ = Float.prototype.isNaN$;
Double.isNaN$D = Double.prototype.isNaN$D = Float.isNaN$F;

Float.prototype.hashCode$ = function() {this._hashcode || (this._hashcode = new String("F\u79d8" + this.valueOf()).hashCode$())}
Double.prototype.hashCode$ = function() {this._hashcode || (this._hashcode = new String("D\u79d8" + this.valueOf()).hashCode$())}
Double.isInfinite$D = Double.prototype.isInfinite$D = Float.isInfinite$F;
Double.prototype.isInfinite$ = Float.prototype.isInfinite$;

m$(Double,"parseDouble$S",
function(s){
if(s == null) {
  throw Clazz.new_(NumberFormatException.c$$S, ["null"]);
}
if(s.length == 0) {
	  throw Clazz.new_(NumberFormatException.c$$S, ["empty String"]);
}
if (s.indexOf("NaN") >= 0)
	return NaN;
var v=Number(s);
if(isNaN(v)){
throw Clazz.new_(NumberFormatException.c$$S, ["Not a Number : "+s]);
}
return v;
}, 1);

m$(Double,["doubleToRawLongBits$D", "doubleToLongBits$D"],
function(d){
	geta64()[0] = d;
	return toLongI2(i64[0], i64[1]);
}, 1);

m$(Double,"valueOf$S",
function(v){
return Clazz.new_(Double.c$$S, [v]);
}, 1);

m$(Double,"valueOf$D",
function(v){
return Clazz.new_(Double.c$, [v]);
}, 1);

// Double.prototype.equals =
m$(Double,"equals$O",
function(s){
return (s instanceof Double) && s.valueOf()==this.valueOf();
});

m$(Double, "$box$", function(v) {
	return Double.valueOf$D(v.doubleValue$());
});


Clazz._setDeclared("Boolean", 
Boolean = java.lang.Boolean = Boolean || function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
});

extendObject(Boolean);

Boolean.__CLASS_NAME__="Boolean";
addInterface(Boolean,[java.io.Serializable,java.lang.Comparable]);
setJ2STypeclass(Boolean, "boolean", "Z");
// extendPrototype(Boolean, true, false);
Boolean.serialVersionUID=Boolean.prototype.serialVersionUID=-3665804199014368530;

m$(Boolean, ["c$", "c$$S"],
function(s){
  var b = ((typeof s == "string" ? Boolean.toBoolean(s) : s) ? true : false);
  this.valueOf=function(){return b;};
}, 1);

m$(Boolean, "c$$Z", function(v){
	this.valueOf=function(){return v;};
	}, 1);


Boolean.TRUE=Boolean.prototype.TRUE=Clazz.new_(Boolean.c$$Z, [true]);
Boolean.FALSE=Boolean.prototype.FALSE=Clazz.new_(Boolean.c$$Z, [false]);
m$(Boolean,"valueOf$S",function(s){	return("true".equalsIgnoreCase$S(s)?Boolean.TRUE:Boolean.FALSE);}, 1);

// the need is to have new Boolean(string), but that won't work with native
// Boolean
// so instead we have to do a lexical switch from "new Boolean" to
// "Boolean.from"
// note no $ here

m$(Boolean,"valueOf$Z",function(b){ return(b?Boolean.TRUE:Boolean.FALSE);}, 1);


// encoded by the transpiler for new Boolean(boolean); NOT equivalent to
// Boolean.TRUE or Boolean.FALSE
m$(Boolean,"from",
function(name){
return Clazz.new_(Boolean.c$$Z, [Boolean.toBoolean(name)]);
}, 1);

m$(Boolean,"getBoolean$S",
function(name){
var result=false;
try{
result=Boolean.toBoolean(System.getProperty$S(name));
}catch(e){
if(Clazz.instanceOf(e,IllegalArgumentException)){
}else if(Clazz.instanceOf(e,NullPointerException)){
}else{
throw e;
}
}
return result;
}, 1);

m$(Boolean,"parseBoolean$S", function(s){return Boolean.toBoolean(s);}, 1);

m$(Boolean,"toBoolean",
function(name){
return(typeof name == "string" ? name.equalsIgnoreCase$S("true") : !!name);
}, 1);


m$(Boolean,["$c","booleanValue","booleanValue$"], function(){ return this.valueOf(); });

m$(Boolean,"compare$Z$Z", function(a,b){return(a == b ? 0 : a ? 1 : -1);}, 1);

m$(Boolean,["compareTo$Boolean","compareTo$O"],
		function(b){
		return(b.valueOf() == this.valueOf() ? 0 : this.valueOf() ? 1 : -1);
		});

// Boolean.prototype.equals =
	m$(Boolean,"equals$O",
		function(obj){
		return obj instanceof Boolean && this.booleanValue()==obj.booleanValue();
		});

m$(Boolean,"hashCode$", function(){ return this.valueOf()?1231:1237;});
m$(Boolean,"hashCode$Z", function(b){ return b?1231:1237;}, 1);

m$(Boolean,"logicalAnd$Z$Z", function(a,b){return(a && b);}, 1);
m$(Boolean,"logicalOr$Z$Z", function(a,b){return(a || b);}, 1);
m$(Boolean,"logicalXor$Z$Z", function(a,b){return !!(a ^ b);}, 1);


m$(Boolean,"toString",function(){return this.valueOf()?"true":"false";});
m$(Boolean,"toString$Z",function(b){return "" + b;}, 1);


Clazz._Encoding={
  UTF8:"utf-8",   // EF BB BF
  UTF16:"utf-16", // FF FE (LE)
  ASCII:"ascii"
};

(function(E) {

	var textDecoder = (self.TextDecoder && new TextDecoder() || null);

E.guessEncoding=function(str){
return ((str.charCodeAt(0)&0xFF)==0xEF&&(str.charCodeAt(1)&0xFF)==0xBB&&(str.charCodeAt(2)&0xFF)==0xBF ? E.UTF8
  : (str.charCodeAt(0)&0xFF)==0xFF&&(str.charCodeAt(1)&0xFF)==0xFE ? E.UTF16 // LE
  : E.ASCII);
};

E.guessEncodingArray=function(a, offset){
return ((a[offset]&0xFF)==0xEF&&(a[offset + 1]&0xFF)==0xBB&&(a[offset + 2]&0xFF)==0xBF ? E.UTF8 
  : (a[offset + 0]&0xFF)==0xFF&&(a[offset + 1]&0xFF)==0xFE ? E.UTF16 : E.ASCII);
};

E.readUTF8Array=function(a, offset, length){
	// a will be an Int8Array, UTF8 only
	// TextDecoder will accept a BOM or not. Java doesn't
  var encoding=E.guessEncodingArray(a, offset);
  var startIdx=0;
  if(encoding==E.UTF8){
	startIdx=3;
  }else if(encoding==E.UTF16){
	startIdx=2;
  }
  if (textDecoder) {
	offset += startIdx;
	length -= startIdx;
	if (offset == 0 && length == a.length)
		return textDecoder.decode(a);
	var arr=new Uint8Array(length);
	for(var i = 0; i < length; i++){
		arr[i] = a[offset + i];
	}
	// Java needs to see the 0xFEFF byte mark
	var s = textDecoder.decode(arr);
	return (startIdx ? '\ufeff' + s : s);
  }
// IE only. I don't know where this comes from. Is it Java?
var arrs=new Array();
for(var i=offset + startIdx, endIdx = offset + length; i < endIdx; i++){
var charCode=a[i];
if(charCode<0x80){
arrs[arrs.length]=String.fromCharCode(charCode);
}else if(charCode>0xc0&&charCode<0xe0){
var c1=charCode&0x1f;
var c2=a[++i]&0x3f;
var c=(c1<<6)+c2;
arrs[arrs.length]=String.fromCharCode(c);
}else if(charCode>=0xe0){
var c1=charCode&0x0f;
var c2=a[++i]&0x3f;
var c3=a[++i]&0x3f;
var c=(c1<<12)+(c2<<6)+c3;
arrs[arrs.length]=String.fromCharCode(c);
}
}
return arrs.join('');
};


E.convert2UTF8=function(str){
var encoding=this.guessEncoding(str);
var startIdx=0;
if(encoding==E.UTF8){
return str;
}else if(encoding==E.UTF16){
startIdx=2;
}

var offset=0;
var arrs=new Array(offset+str.length-startIdx);

for(var i=startIdx;i<str.length;i++){
var charCode=str.charCodeAt(i);
if(charCode<0x80){
arrs[offset+i-startIdx]=str.charAt(i);
}else if(charCode<=0x07ff){
var c1=0xc0+((charCode&0x07c0)>>6);
var c2=0x80+(charCode&0x003f);
arrs[offset+i-startIdx]=String.fromCharCode(c1)+String.fromCharCode(c2);
}else{
var c1=0xe0+((charCode&0xf000)>>12);
var c2=0x80+((charCode&0x0fc0)>>6);
var c3=0x80+(charCode&0x003f);
arrs[offset+i-startIdx]=String.fromCharCode(c1)+String.fromCharCode(c2)+String.fromCharCode(c3);
}
}
return arrs.join('');
};
if(!String.__PARAMCODE){

String.__PARAMCODE = "S";

Clazz._setDeclared("String", java.lang.String=String);

extendObject(String, EXT_NO_TOSTRING);
 
addInterface(String,[java.io.Serializable,CharSequence,Comparable]);

String.serialVersionUID=String.prototype.serialVersionUID=-6849794470754667710;

var formatterClass;

String.format$S$OA = function(format, args) {
  if (!formatterClass)
    formatterClass = Clazz._4Name("java.util.Formatter", null, null, true);
  var f = new formatterClass();
  return f.format$S$OA.apply(f,arguments).toString();
 };

 // Java8
 String.lastIndexOf$CA$I$I$S$I = function(source, sourceOffset, sourceCount, target, fromIndex) {
	return C$.lastIndexOf$CA$I$I$CA$I$I$I(source, sourceOffset, sourceCount, target.value, 0, target.value.length, fromIndex);
 };
 
 // Java8
 String.lastIndexOf$CA$I$I$CA$I$I$I = function(source, sourceOffset, sourceCount, target, targetOffset, targetCount, fromIndex) {
	var rightIndex=sourceCount - targetCount;
	if (fromIndex < 0) {
	return -1;
	}if (fromIndex > rightIndex) {
	fromIndex=rightIndex;
	}if (targetCount == 0) {
	return fromIndex;
	}var strLastIndex=targetOffset + targetCount - 1;
	var strLastChar=target[strLastIndex];
	var min=sourceOffset + targetCount - 1;
	var i=min + fromIndex;
	searching : while (true){
	while (i >= min && source[i] != strLastChar ){
	i--;
	}
	if (i < min) {
	return -1;
	}var j=i - 1;
	var start=j - (targetCount - 1);
	var k=strLastIndex - 1;
	while (j > start){
	if (source[j--] != target[k--]) {
	i--;
	continue searching;
	}}
	return start - sourceOffset + 1;
	}
	};

 
 String.CASE_INSENSITIVE_ORDER = {
	compare$O$O: function(s1, s2){
		if(s1==null || s2 == null)
			throw new NullPointerException();
		if(s1==s2) return 0;
		var s1=s1.toUpperCase();
		var s2=s2.toUpperCase();
		if(s1==s2) return 0;
		var s1=s1.toLowerCase();
		var s2=s2.toLowerCase();
		return (s1==s2 ? 0 : s1 > s2 ? 1 : -1);
	}
 } 
 
String.CASE_INSENSITIVE_ORDER.compare$S$S = String.CASE_INSENSITIVE_ORDER.compare$O$O;

CharSequence.$defaults$(String);
 
;(function(sp) {

	// Java-11

sp.isBlank$ = function() {
	return this.indexOfNonWhitespace$() == this.length$();	
}

sp.lines$ = function() {
	return CharSequence.lines$S(this);
}

sp.indexOfNonWhitespace$ = function() {
	return this.length - this.stripLeading$().length;
}

//sp.chars$ is implemented as CharSequence.prototype.chars$
//sp.codePoints$ is implemented as = CharSequence.prototype.codePoints$

sp.repeat$I = function(count) {
    if (count < 0) {
        throw new IllegalArgumentException("count is negative: " + count);
    }
    if (count == 1) {
        return this;
    }
    var len = this.length;
    if (len == 0 || count == 0) {
        return "";
    }
    var s = this;
    for (var i = 1; i < count; i++) {
    	s += this;
    }
    return s;
}

sp.strip$ = function() { return this.trim(); }

sp.stripLeading$ = function() { return this.trimStart ? this.trimStart() : this.trimLeft(); }

sp.stripTrailing$ = function() { return this.trimEnd ? this.trimEnd() : this.trimRight(); }

	//
sp.compareToIgnoreCase$S = function(str) { return String.CASE_INSENSITIVE_ORDER.compare$S$S(this, str);}

sp.replace$ = function(c1,c2){
  if (c1 == c2 || this.indexOf(c1) < 0) return "" + this;
  if (c1.length == 1) {
    if ("\\$.*+|?^{}()[]".indexOf(c1) >= 0)   
      c1 = "\\" + c1;
  } else {    
    c1=c1.replace(/([\\\$\.\*\+\|\?\^\{\}\(\)\[\]])/g,function($0,$1){return "\\"+$1;});
  }
  return this.replace(new RegExp(c1,"gm"),c2);
};

// fastest:
sp.replaceAll$=sp.replaceAll$S$S=sp.replaceAll$CharSequence$CharSequence=function(exp,str){
return this.replace(newRegExp(exp,"gm"),str);
};
sp.replaceFirst$S$S=function(exp,str){
return this.replace(newRegExp(exp,"m"),str);
};
sp.matches$S=function(exp){
if(exp!=null){
exp="^("+exp+")$";
}
var regExp=newRegExp(exp,"gm");
var m=this.match(regExp);
return m!=null&&m.length!=0;
};

sp.regionMatches$I$S$I$I=function(toffset,other,ooffset,len){
  return this.regionMatches$Z$I$S$I$I(false,toffset,other,ooffset,len);
}

sp.regionMatches$Z$I$S$I$I=function(ignoreCase,toffset,other,ooffset,len){
var to=toffset;
var po=ooffset;

if((ooffset<0)||(toffset<0)||(toffset>this.length-len)||
(ooffset>other.length-len)){
return false;
}
var s1=this.substring(toffset,toffset+len);
var s2=other.substring(ooffset,ooffset+len);
if(ignoreCase){
s1=s1.toLowerCase();
s2=s2.toLowerCase();
}
return s1==s2;
};

var newRegExp = function(regex, flags) {
	if (regex.indexOf("\\Q") >= 0 || regex.indexOf("(?") == 0)
		return Clazz.loadClass("java.util.regex.Pattern").getJSRegex$S$S(regex, flags);
	return new RegExp(regex, flags);
}
sp.split$S=sp.split$S$I=function(regex,limit){
var arr;
if (!limit && regex == " ") {
	arr = this.split(" ");
} else if(limit && limit > 0){
	if(limit == 1){
	arr = [this];
	} else {
		var regExp=newRegExp("("+regex+")","gm");
		var count=1;
		var s=this.replace(regExp,function($0,$1){
			count++;
			if(count==limit){
				return"@@_@@";
			}
			else if(count>limit){
				return $0;
			}else{
				return $0;
			}
		});
		regExp=new RegExp(regex,"gm");
		arr=this.split(regExp);
		if(arr.length>limit){
			arr[limit-1]=s.substring(s.indexOf("@@_@@")+5);
			arr.length=limit;
		}
	}
}else{
	arr = this.split(newRegExp(regex,"gm"));
}
while (arr[arr.length - 1] === "")
	arr.pop();
return Clazz.array(String, -1, arr);
};

var sn=function(s, prefix,toffset){
  var to=toffset;
  var po=0;
  var pc=prefix.length;
  
  if((toffset<0)||(toffset>s.length-pc)){
  return false;
  }
  while(--pc>=0){
  if(s.charAt(to++)!=prefix.charAt(po++)){
  return false;
  }
  }
  return true;
};

sp.startsWith$S=sp.startsWith$S$I=function(prefix){
if(arguments.length==1){
return sn(this,arguments[0],0);
}else if(arguments.length==2){
return sn(this,arguments[0],arguments[1]);
}else{
return false;
}
};

sp.endsWith$S=function(suffix){
return sn(this, suffix,this.length-suffix.length);
};

sp.equals$O = function(anObject){
return this.valueOf()==anObject;
};

sp.equalsIgnoreCase$S=function(anotherString){
return(anotherString==null)?false:(this==anotherString
||this.toLowerCase()==anotherString.toLowerCase());
};


sp.hash=0;

sp.hashCode$=function(){
var h=this.hash;
if(h==0){
var off=0;
var len=this.length;
for(var i=0;i<len;i++){
h=31*h+this.charCodeAt(off++);
h&=0xffffffff;
}
this.hash=h;
}
return h;
};

sp.getChars$I$I$CA$I=function(srcBegin,srcEnd,dst,dstBegin){
	getChars(this, srcBegin, srcEnd, dst, dstBegin, false);
};

sp.getChars$I$I$BA$I=function(srcBegin,srcEnd,dst,dstBegin){
	getChars(this, srcBegin, srcEnd, dst, dstBegin, true);
};

var getChars = function(s, srcBegin,srcEnd,dst,dstBegin, asBytes){
	if(srcBegin<0){
	throw new StringIndexOutOfBoundsException(srcBegin);
	}
	if(srcEnd>s.length){
	throw new StringIndexOutOfBoundsException(srcEnd);
	}
	if(srcBegin>srcEnd){
	throw new StringIndexOutOfBoundsException(srcEnd-srcBegin);
	}
	if(dst==null){
	throw new NullPointerException();
	}
	for(var i=0;i<srcEnd-srcBegin;i++){
		dst[dstBegin+i]=(asBytes ? s.charCodeAt(srcBegin+i) : s.charAt(srcBegin+i));
	}
};

// var
// charset=["utf-8","utf8","us-ascii","iso-8859-1","8859_1","gb2312","gb18030"];
var charset=["utf-8","utf8","us-ascii","iso-8859-1"]; // gb* uses GBK

sp.getBytes$I$I$BA$I=function(i0, i1, dst, dpt) {
	if (i1 == i0)
		return;
	var s = this.valueOf();
	for (var i = i0; i < i1; i++)
		dst[dpt++] = s.charCodeAt(i);
}

sp.getBytes$=sp.getBytes$S=sp.getBytes$java_nio_charset_Charset=function(){
var s=this;
var cs = (arguments.length == 1 ? arguments[0] : "utf-8").toString().toLowerCase();
 var simple=false;
 for(var i=0;i<charset.length;i++){
  if(charset[i]==cs){
   simple=true;
   break;
  }
 }
 if(!simple){
  cs = arguments[0];
  if (typeof cs == "string")
   cs = Clazz.loadClass("java.nio.charset.Charset").forName$S(cs);
  if (!cs)
	throw new java.io.UnsupportedEncodingException();
  return cs.encode$S(this.toString()).toArray$();	
 }
 if(cs=="utf-8"||cs=="utf8"){
  s=E.convert2UTF8(this);
 }
var arrs=[];
for(var i=0, ii=0;i<s.length;i++){
var c=s.charCodeAt(i);
if(c>255){
arrs[ii]=0x1a;
arrs[ii+1]=c&0xff;
arrs[ii+2]=(c&0xff00)>>8;
ii+=2;
}else{
arrs[ii]=c;
}
ii++;
}
return Clazz.array(Byte.TYPE, -1, arrs);
};

sp.contains$S = function(a) {return this.indexOf(a) >= 0}  // bh added
sp.compareTo$ = sp.compareTo$S = sp.compareTo$O = function(a){return this > a ? 1 : this < a ? -1 : 0} // bh
																										// added

sp.toCharArray$=function(){
	var result = this.split("");	
	return setArray(result, Character.TYPE, "CA", -1);
};

String.valueOf$ = String.valueOf$Z = String.valueOf$C = String.valueOf$CA 
				= String.valueOf$CA$I$I = String.valueOf$D = String.valueOf$F 
				= String.valueOf$I = String.valueOf$J = String.valueOf$O = 
function(o){
if(o=="undefined"){
return String.valueOf();
}
if(o instanceof Array){
if(arguments.length==1){
return o.join('');
}else{
var off=arguments[1];
var len=arguments[2];
var oo=new Array(len);
for(var i=0;i<len;i++){
oo[i]=o[off+i];
}
return oo.join('');
}
}
return (o != null && o.toString ? o.toString() : ""+o);
};

sp.subSequence$I$I=function(beginIndex,endIndex){
return this.substring(beginIndex,endIndex);
};

sp.contentEquals$CharSequence=sp.contentEquals$StringBuffer=function(cs){
	return cs && (cs.toString() == this);
};

sp.contains$CharSequence=function(cs){
if(cs==null)
  throw new NullPointerException();
return (this == cs || this.length > cs.length$() && this.indexOf(cs.toString()) > -1);
};

sp.contentEquals$CharSequence=function(cs){
if(cs==null)
  throw new NullPointerException();
if(this == cs)
 return true;
if(this.length!=cs.length$())
 return false;
var v=cs.getValue();
var n=this.length;
while(n-- >= 0){
  if(this.charCodeAt(n)!=v[n]){
    return false;
  }
}
return true;
};

sp.concat$S = function(s){
if(s==null){
throw new NullPointerException();
}
return this.concat(s);
};

sp.isEmpty$ = function() {
  return this.valueOf().length == 0;
}

sp.indexOf$S = sp.indexOf$S$I = sp.indexOf;
sp.lastIndexOf$S = sp.lastIndexOf;

sp.indexOf$I = function(c){
	return this.indexOf(typeof c == "string" ? c : String.fromCodePoint(c));
};

sp.indexOf$I$I = function(c, first) {
	return this.indexOf(typeof c == "string" ? c : String.fromCodePoint(c), first);
}

sp.lastIndexOf$S = sp.lastIndexOf$S$I = sp.lastIndexOf;

sp.lastIndexOf$I = function(c){
	return this.lastIndexOf(typeof c == "string" ? c : String.fromCodePoint(c));
};

sp.lastIndexOf$I$I = function(c, last) {
	return this.lastIndexOf(typeof c == "string" ? c : String.fromCodePoint(c), last);
}

sp.intern$=function(){
return this.valueOf();
};

String.copyValueOf$S=String.copyValueOf$S$I$I=sp.copyValueOf=function(){
  if(arguments.length==1){
  return String.instantialize(arguments[0]);
  }else{
  return String.instantialize(arguments[0],arguments[1],arguments[2]);
  }
};

sp.$c = function(){return this.charCodeAt(0)};

// covers for same functions in JavaScript
sp.codePointAt$I = (sp.codePointAt || sp.charCodeAt); // MSIE only
sp.charCodeAt$I = sp.charCodeAt;
sp.charAt$I = sp.charAt;
sp.substring$I = sp.substring$I$I = sp.subSequence$I$I = sp.substring;
sp.replace$C$C = sp.replace$CharSequence$CharSequence = sp.replace$;
sp.toUpperCase$ = sp.toUpperCase;
sp.toLowerCase$ = sp.toLowerCase;
sp.toLowerCase$java_util_Locale = sp.toLocaleLowerCase ? function(loc) {loc = loc.toString(); var s = this.valueOf(); return (loc ? s.toLocaleLowerCase(loc.replace(/_/g,'-')) : s.toLocaleLowerCase()) } : sp.toLowerCase;
sp.toUpperCase$java_util_Locale = sp.toLocaleUpperCase ? function(loc) {loc = loc.toString(); var s = this.valueOf(); return (loc ? s.toLocaleUpperCase(loc.replace(/_/g,'-')) : s.toLocaleUpperCase()) } : sp.toUpperCase;
sp.length$ = function() {return this.length};
sp.trim$ = function() {
  var s = this.trim();
  var j;
  if (s == "" || s.charCodeAt(j = s.length - 1) > 32 && s.charCodeAt(0) > 32) return s;
  var i = 0;
  while (i <= j && s.charCodeAt(i) <= 32)i++;
  while (j > i && s.charCodeAt(j) <= 32)j--;
  return s.substring(i, ++j);
};


// toString is always unqualified, and it is unnecessary for String


})(String.prototype);

// Note that of all these constructors, only new String("xxx") and new
// String(new String())
// return actual JavaScript String objects (as of 3.2.9.v1)

String.instantialize=function(){
var x=arguments[0];
switch (arguments.length) {
case 0:
  return new String();
case 1:
  // String(byte[] bytes)
  // String(char[] value)
  // String(StringBuffer buffer)
  // String(StringBuilder builder)
  // String(String original)
  if (x.__BYTESIZE){
    return x.length == 0 ? "" : E.readUTF8Array(x, 0, x.length).toString();
  }
  if (x instanceof Array){
	    return x.length == 0 ? "" : typeof x[0]=="number" ? E.readUTF8Array(new Uint8Array(x), 0, x.length).toString() : x.join('');
  }
  // raw JavaScript string unless new String(string)
  return (typeof x == "string" ||  x instanceof String ? new String(x) : x.toString());
case 2:  
  // String(byte[] ascii, int hibyte)
  // String(char[] value, boolean share) ???
  // String(byte[] bytes, Charset charset)
  // String(byte[] bytes, String charsetName)

  var a1=arguments[1];
  return (typeof a1=="number" ? String.instantialize(x,a1,0,x.length) 
	: typeof a1 == "boolean" ? x.join('') 
    : String.instantialize(x,0,x.length,a1.toString()));
case 3:
  // String(byte[] bytes, int offset, int length)
  // String(char[] value, int offset, int count)
  // String(int[] codePoints, int offset, int count)

  var bytes=x;
  var offset=arguments[1];
  var length=arguments[2];
  if(offset<0||length+offset>bytes.length){
	    throw new IndexOutOfBoundsException();
  }
  if (length == 0)
	  return "";
  var arr=new Array(length);
  var isChar=!!bytes[offset].length;
  if(isChar){
      for(var i=0;i<length;i++){
        arr[i]=bytes[offset+i];
      }
  }else{
      for(var i=0;i<length;i++){
        arr[i]=String.fromCharCode(bytes[offset+i]);
      }
  }
  return arr.join('');
case 4:
  // String(byte[] bytes, int offset, int length, Charset charset)
  // String(byte[] bytes, int offset, int length, String charsetName)
  // String(byte[] ascii, int hibyte, int offset, int count)

  var bytes=x;
  var cs=arguments[3];
  if(typeof cs != "number"){
    var offset=arguments[1];
    var length=arguments[2];
    if (typeof cs == "string") {
    	if (",utf8,utf-8,".indexOf("," + cs.toLowerCase() + ",") >= 0)
    		return E.readUTF8Array(bytes,offset,length).toString();
    	cs = Clazz.loadClass("java.nio.charset.Charset").forName$S(cs);
    	if (!cs)
    		throw new java.io.UnsupportedEncodingException();
    }
    return cs.decode$BA$I$I(bytes, offset, length).toString();
  }
  var count=arguments[3];
  var offset=arguments[2];
  var hibyte=arguments[1];
  var value=new Array(count);
  if(hibyte==0){
    for(var i=count;i-->0;){
      value[i]=String.fromCharCode(bytes[i+offset]&0xff);
    }
  }else{
    hibyte<<=8;
    for(var i=count;i-->0;){
      value[i]=String.fromCharCode(hibyte|(bytes[i+offset]&0xff));
    }
  }
  return value.join('');
default:
  // ????
  var s="";
  for(var i=0;i<arguments.length;i++){
    s+=arguments[i];
  }
  return s;
}
};

}

})(Clazz._Encoding);

String.copyValueOf$CA$I$I = function(data,offset,count) {
 var s = "";
 for (var pt = offset, n = offset+count;pt < n;pt++)s += data[pt];
 return s;
}
String.copyValueOf$CA = function(data) {
 return sp.copyValueOf$CA$I$I(data, 0, data.length);
}

// Java8
String.join$CharSequence$CharSequenceA = function(sep,array) {
 var ret = "";
 var s = "";
 for (var i = 0; i < array.length; i++) {
	ret += s + array[i].toString();
	s || (s = sep);	
 }
 return ret;
}

// Java8
String.join$CharSequence$Iterable = function(sep,iterable) {
 var ret = "";
 var s = "";
 var iter = iterable.iterator$();
 while (iter.hasNext$()) {
	ret += s + iter.next$().toString();
	s || (s = sep);	
 }
 return ret;
}
 
var C$=Clazz.newClass(java.lang,"Character",function(){
if (arguments[0] === null || typeof arguments[0] != "object")this.c$(arguments[0]);
},null,[java.io.Serializable,Comparable]);
Clazz._setDeclared("Character", java.lang.Character); 
setJ2STypeclass(Character, "char", "C");

var unicode_txt="";

m$(C$,"getName$I",
function(codePoint){
	if (!unicode_txt) {
		try {
			unicode_txt = J2S.getFileData(ClassLoader.getClasspathFor("java.lang",1)  + "org/unicode/public/unidata/NamesList.txt");
		} catch (e) {
			return "??";
		}
	}
	var code = "0000" + Integer.toHexString$I(codePoint);
	code = code.substring(code.length - 4).toUpperCase();
	var pt = unicode_txt.indexOf("\n" + code + "\t");
	if (pt < 1)
		return "\\u" + code;
	var pt1 = unicode_txt.indexOf("\n", pt + 1);
	return (pt1 < 0 ? "??" : unicode_txt.substring(pt + 6, pt1));
}, 1);

m$(C$,"valueOf$C",function(c){
        return (c <= '\u007F' ? getCachedNumber(c, chars, Character, "c$$C")
        		: Clazz.new_(Character.c$$C, [c]));
},1);

C$.prototype.$c = function(){return this.value.charCodeAt(0)};


m$(C$,["c$", "c$$C"],
function(value){
this.value=value;
this.valueOf=function(){return value};
}, 1);

m$(C$,["charValue", "charValue$"],
function(){
return this.value;
});

m$(C$,"hashCode$",
function(){
return(this.value).charCodeAt(0);
});
m$(C$,"equals$O",
function(obj){
if(Clazz.instanceOf(obj,Character)){
return this.value.charCodeAt(0)==obj.value.charCodeAt(0);
}return false;
});

m$(C$, "$box$", function(c) {
	return Character.valueOf$C(typeof c == "string" ? c : String.fromCharCode(c));
});

m$(C$, "$incr$",function(n){return this.$box$(this.value.charCodeAt(0) + n);});


m$(C$,"charCodeAt$I",
function(i){
return(this.value).charCodeAt(i);
});
m$(C$,"isValidCodePoint$I",
function(i){
	try {
	String.fromCodePoint(i);
	return true;
	} catch(e) {
	return false;
	}
});

m$(C$,["compareTo$C","compareTo$","compareTo$O"],
function(c){
return(this.value).charCodeAt(0)-(c.value).charCodeAt(0);
});
m$(C$,"toLowerCase$C",
function(c){
return(""+c).toLowerCase().charAt(0);
}, 1);
m$(C$,"toTitleCase$C",
function(c){
  return Character.toUpperCase$C(c);
}, 1);
m$(C$,"toUpperCase$C",
function(c){
return(""+c).toUpperCase().charAt(0);
}, 1);
m$(C$,"toLowerCase$I",
function(i){
return String.fromCodePoint(i).toLowerCase().codePointAt(0);
}, 1);
m$(C$,"toTitleCase$I",
function(i){
return String.fromCodePoint(i).toTitleCase().codePointAt(0);
}, 1);
m$(C$,"toUpperCase$I",
function(i){
return String.fromCodePoint(i).toUpperCase().codePointAt(0);
}, 1);
m$(C$,["isDigit$C","isDigit$I"],
function(c){
	if (typeof c == "string")
		c = c.charCodeAt(0);
return (48 <= c && c <= 57);
}, 1);

m$(C$,["isISOControl$C", "isISOControl$I"],
function(c){
if (typeof c == "string")
  c = c.charCodeAt(0);
return (c < 0x1F || 0x7F <= c && c <= 0x9F);
}, 1);


m$(C$,"isAlphabetic$I", function(c){return Character.isLetter$I(c)}, 1);

// A character may be part of a Java identifier if any of the following are
// true:
//
// it is a letter
// it is a currency symbol (such as '$')
// it is a connecting punctuation character (such as '_')
// it is a digit
// it is a numeric letter (such as a Roman numeral character)
// it is a combining mark
// it is a non-spacing mark
// isIdentifierIgnorable returns true for the character
    
    
m$(C$,["isJavaIdentifierStart$C","isJavaIdentifierStart$I"],
		function(c){
	if (typeof c == "string")
		c = c.charCodeAt(0);
	// letter, $, _,
	return Character.isLetter$I(c) || c == 0x24 || c == 0x5F
		}, 1);


m$(C$,["isJavaIdentifierPart$C","isJavaIdentifierPart$I"],
		function(c){
	if (typeof c == "string")
		c = c.charCodeAt(0);
	// letter, digit $, _,
	return Character.isLetterOrDigit$I(c) || c == 0x24 || c == 0x5F
		}, 1);


m$(C$,["isLetter$C", "isLetter$I"],
function(c){
if (typeof c == "string")
  c = c.charCodeAt(0);
return (65 <= c && c <= 90 || 97 <= c && c <= 122);
}, 1);
m$(C$,["isLetterOrDigit$C","isLetterOrDigit$I"],
function(c){
if (typeof c == "string")
  c = c.charCodeAt(0);
return (65 <= c && c <= 90 || 97 <= c && c <= 122 || 48 <= c && c <= 57);
}, 1);
m$(C$,["isLowerCase$C","isLowerCase$I"],
function(c){
if (typeof c == "string")
    c = c.charCodeAt(0);
return (97 <= c && c <= 122);
}, 1);
m$(C$,"isSpace$C",
function(c){
 var i = c.charCodeAt(0);
 return (i==0x20||i==0x9||i==0xA||i==0xC||i==0xD);
}, 1);
m$(C$,["isSpaceChar$C","isSpaceChar$I"],
function(c){
 var i = (typeof c == "string" ? c.charCodeAt(0) : c);
if(i==0x20||i==0xa0||i==0x1680)return true;
if(i<0x2000)return false;
return i<=0x200b||i==0x2028||i==0x2029||i==0x202f||i==0x3000;
}, 1);
m$(C$,["isTitleCase$C","isTitleCase$I"],
function(c){
  return Character.isUpperCase$C(c);
}, 1);
m$(C$,["isUpperCase$C","isUpperCase$I"],
function(c){
if (typeof c == "string")
  c = c.charCodeAt(0);
return (65 <= c && c <= 90);
}, 1);
m$(C$,["isWhitespace$C","isWhitespace$I"],
function(c){
if (typeof c == "string")
 c = c.charCodeAt(0);
return (c >= 0x1c && c <= 0x20 || c >= 0x9 && c <= 0xd || c == 0x1680
  || c >= 0x2000 && c != 0x2007 && (c <= 0x200b || c == 0x2028 || c == 0x2029 || c == 0x3000));
}, 1);
m$(C$,"isSurrogate$C", function(c) {
	c = c.charCodeAt(0);
	return c >= 0xd800 && c < 0xe000;
	
}, 1);
m$(C$,"isHighSurrogate$C", function(c) {
	c = c.charCodeAt(0);
	return c >= 0xd800 && c < 0xdc00;
	
}, 1);
m$(C$,"isLowSurrogate$C", function(c) {
	c = c.charCodeAt(0);
	return c >= 0xdc00 && c < 0xe000;
	
}, 1);

m$(C$,["digit$C$I","digit$I$I"],
function(c,radix){
var i = (typeof c == "string" ? c.charCodeAt(0) : c);
if(radix >= 2 && radix <= 36){
  if(i < 128){
    var result = -1;
    if(48 <= i && i <= 57){
    result = i - 48;
    }else if(97 <= i && i <= 122){
    result = i - 87;
    }else if(65 <= i && i <= 90){
    result=i-(55);
    }
    return (result < radix ? result : -1);
  }
}
return -1;
}, 1);

m$(C$,"toString$C", function(c) {
	return c;
}, 1);

m$(C$,"toString",
function(c){
if (arguments.length == 0) {
  if(this===Character){
    return"class java.lang.Character";
  }
  var buf=[this.value];
  return String.valueOf$(buf);
}
return String.valueOf$(c);
}, 1);
m$(C$,"charCount$I", function(codePoint){
  return codePoint >= 0x010000 ? 2 : 1;
}, 1);


Integer.compare$I$I = 
Byte.compare$B$B =
Short.compare$H$H =
Float.compare$F$F =
Double.compare$D$D = function(a,b) { return (a < b ? -1 : a == b ? 0 : 1) };

Integer.prototype.objectValue$ = 
Byte.prototype.objectValue$ = 
Short.prototype.objectValue$ = 
Long.prototype.objectValue$ =  
Float.prototype.objectValue$ = 
Boolean.prototype.objectValue$ = 
Double.prototype.objectValue$ =  function() {return this.valueOf()};

Character.prototype.objectValue$ = function() { return this.value };

Character.prototype.intValue$  = function() { return this.value.codePointAt(0) };

Integer.min$I$I = Long.min$J$J = Float.min$F$F = Double.min$D$D = 	function(a,b) { return Math.min(a,b); };

Integer.max$I$I = Long.max$J$J = Float.max$F$F = Double.max$D$D = 	function(a,b) { return Math.max(a,b); };

Integer.sum$I$I = Long.sum$J$J = Float.sum$F$F = Double.sum$D$D = 		function(a,b) { return a + b; };


// TODO: Only asking for problems declaring Date. This is not necessary

// NOTE THAT java.util.Date, like java.lang.Math, is unqualified by the
// transpiler -- this is NOT necessary

;(function() {

Clazz._setDeclared("java.util.Date", java.util.Date=Date);
// Date.TYPE="java.util.Date";
Date.__CLASS_NAME__="Date";
addInterface(Date,[java.io.Serializable,java.lang.Comparable]);

Date.parse$S = Date.parse;

m$(java.util.Date, ["c$", "c$$S", "c$$J"], function(t) {
  this.setTime$J(typeof t == "string" ? Date.parse(t) : t ? t : System.currentTimeMillis$())
}, 1);

m$(java.util.Date, ["getClass$", "getClass"], function () { return Clazz.getClass(this); }, 1);

m$(java.util.Date,["clone$","clone"],
function(){
return new Date(this.getTime());
});

m$(java.util.Date,["before", "before$java_util_Date"],
function(when){
return this.getTime()<when.getTime();
});
m$(java.util.Date,["after", "after$java_util_Date"],
function(when){
return this.getTime()>when.getTime();
});

m$(java.util.Date,["equals","equals$O"],
function(obj){
return Clazz.instanceOf(obj,java.util.Date)&&this.getTime()==(obj).getTime();
});
m$(java.util.Date,["compareTo","compareTo$java_util_Date","compareTo$","compareTo$O","compareTo$O"],
function(anotherDate){
var thisTime=this.getTime();
var anotherTime=anotherDate.getTime();
return(thisTime<anotherTime?-1:(thisTime==anotherTime?0:1));
});
m$(java.util.Date,["hashCode","hashCode$"],
function(){
var ht=this.getTime();
return parseInt(ht)^parseInt((ht>>32));
});

Date.prototype.toString$ = Date.prototype.toString$$ = Date.prototype.toString;
m$(java.util.Date,"toString",
function(){
var a = this.toString$().split(" ");
// Sun Mar 10 1996 17:05:00 GMT-0600 (Central Daylight Time) -> Sun Mar 10
// 16:05:00 CST 1996
return a[0] + " " + a[1] + " " + a[2] + " " + a[4] + " " + a[5] + " " + a[3];
// return this.toString$().split("(")[0].trim();
});
})();

var notImplemented = function(why) {return function() {System.err.println(why + " has not been implemented.")}};

;(function(dp){
dp.from$java_time_Instant = notImplemented("Date.from(java.time.Instant)");
dp.setInstant$ = notImplemented("Date.toInstant()");

dp.getDate$ = dp.getDate;
dp.getDay$ = dp.getDay;
dp.getHours$ = dp.getHours;
dp.getMinutes$ = dp.getMinutes;
dp.getMonth$ = dp.getMonth;
dp.getSeconds$ = dp.getSeconds;
dp.getTime$ = dp.getTime;
dp.getTimeZoneOffset$ = dp.getTimeZoneOffset;
dp.getYear$ = dp.getYear;
dp.parse$S = dp.parse;
dp.setDate$I = dp.setDate;
dp.setHours$I = dp.setHours;
dp.setMinutes$I = dp.setMinutes;
dp.setMonth$I = dp.setMonth;
dp.setSeconds$I = dp.setSeconds;
dp.setTime$J = dp.setTime;
dp.setYear$I = dp.setYear;
dp.toGMTString$ = dp.toUTCString || dp.toGMTString;
dp.toLocaleString$ = dp.toLocaleString = dp.toLocaleDateString;
dp.UTC$ = dp.UTC;


	
})(Date.prototype);

var printStackTrace = function(e, ps) {
	  ps.println$O("" + e);
	  if (e.stackTrace){
		 for (var i = 0; i < e.stackTrace.length; i++) {
		  var t = e.stackTrace[i];
		  if (t.nativeClazz == null || !isInstanceOf(t.nativeClazz, Throwable)) {
		   ps.println$O(t);
		  }
	     }
	  }
	  if (e.stack) {
		  var S = e.stack.split("\n");
		  for (var i = 0; i < S.length; i++) {
			  if (S[i].indexOf("__startAppletJS") >= 0)
				  break;
			  ps.println$O(S[i]);	  
		  }
	  }
}

var C$ = Clazz.newClass(java.lang, "Throwable", function () {
Clazz.newInstance(this, arguments);
}, null, java.io.Serializable);

m$(C$, 'c$', function () {
this.fillInStackTrace$();
this.detailMessage = this.stack;
this.cause = this;
}, 1);

m$(C$, 'c$$S', function (message) {
this.fillInStackTrace$();
this.cause = this;
this.detailMessage = message;
}, 1);

m$(C$, 'c$$S$Throwable', function (message, cause) {
this.fillInStackTrace$();
this.detailMessage = message;
this.cause = cause;
}, 1);

m$(C$, 'c$$Throwable', function (cause) {
this.fillInStackTrace$();
this.detailMessage = (cause == null ? this.stack : cause.toString ());
this.cause = cause;
}, 1);

m$(C$, 'getMessage$', function () {return this.message || this.detailMessage || null});

m$(C$, 'getLocalizedMessage$', function () {
return this.getMessage$();
});

m$(C$, 'getCause$', function () {
return (this.cause === this ? null : this.cause);
});

m$(C$, 'initCause$Throwable', function (cause) {
if (this.cause !== this) throw Clazz.new_(IllegalStateException.c$$S,["Can't overwrite cause"]);
if (cause === this) throw Clazz.new_(IllegalArgumentException.c$$S,["Self-causation not permitted"]);
this.cause = cause;
return this;
});

m$(C$, 'toString', function () {
var s = this.getClass$().getName$();
var message = this.getLocalizedMessage$();
return (message != null) ? (s + ": " + message) : s;
});

m$(C$, 'getStackTrace$', function () {
return this.stackTrace;
});

m$(C$, 'printStackTrace$', function () {
	printStackTrace(this, System.err);
});

m$(C$, 'printStackTrace$java_io_PrintWriter', function (writer) {
	printStackTrace(this, writer);
});

m$(C$, ['printStackTrace$java_io_PrintStream','printStackTrace$java_io_PrintWriter'], function (stream) {
  printStackTrace(this, stream);
});

Clazz.newMeth(C$, 'fillInStackTrace$', function () {
this.stackTrace = Clazz.array(StackTraceElement);
try {
var caller = arguments.callee.caller;
var i = 0;
while (caller.caller && caller.caller.name != "__loadClazz") {
	caller = caller.caller;
	if (++i > 2 && caller.exClazz || caller == Clazz.load)
		break;
}
var superCaller = null;
var callerList = [];
var index = 0;
while (index < 20 && caller != null) {
  index++;
  var clazzName = null;
  var nativeClazz = null;
  superCaller = caller;
  if (superCaller.exClazz == null) {
	  if (superCaller.j2sname ==  "__START_APPLET__")
		  break;
  } else {
    nativeClazz = superCaller.exClazz;
  }
  var st =Clazz.new_(StackTraceElement.c$, [
    ((nativeClazz != null && nativeClazz.__CLASS_NAME__.length != 0) ?
    nativeClazz.__CLASS_NAME__ : "anonymous"),
    ((superCaller.exName == null) ? "anonymous" : superCaller.exName),
    null, -1]);    
  st.nativeClazz = nativeClazz;
  this.stackTrace.push(st);
  for (var i = 0; i < callerList.length; i++) {
    if (callerList[i] == superCaller) {
      // ... stack Information lost as recursive invocation existed ...
      var st =Clazz.new_(StackTraceElement.c$, ["lost", "missing", null, -3]);
      st.nativeClazz = null;
      this.stackTrace.push(st);
      index = 100;
      break;
    }
  }
  if (superCaller != null) {
    callerList.push(superCaller);
  }
  caller = (superCaller && superCaller.arguments && superCaller.arguments.callee) ? superCaller.arguments.callee.caller : null;
}
} catch (e) {};

Clazz.initializingException = false;
return this;
});

Clazz.newMeth(C$, 'setStackTrace$StackTraceElementA', function (stackTrace) {
var defensiveCopy = stackTrace.clone$();
for (var i = 0; i < defensiveCopy.length; i++) if (defensiveCopy[i] == null) throw Clazz.new_(NullPointerException.c$$S,["stackTrace[" + i + "]"]);

this.stackTrace = defensiveCopy;
});

C$=Clazz.newClass(java.lang,"StackTraceElement",function(){
this.declaringClass=null;
this.methodName=null;
this.fileName=null;
this.lineNumber=0;
},null,java.io.Serializable);

m$(C$, "c$",function(cls,method,file,line){
if(cls==null||method==null){
throw new NullPointerException();
}this.declaringClass=cls;
this.methodName=method;
this.fileName=file;
this.lineNumber=line;
},1);

m$(C$,"equals$O",
function(obj){
if(!(Clazz.instanceOf(obj,StackTraceElement))){
return false;
}var castObj=obj;
if((this.methodName==null)||(castObj.methodName==null)){
return false;
}if(!this.getMethodName$().equals(castObj.getMethodName$())){
return false;
}if(!this.getClassName$().equals(castObj.getClassName$())){
return false;
}var localFileName=this.getFileName$();
if(localFileName==null){
if(castObj.getFileName$()!=null){
return false;
}}else{
if(!localFileName.equals(castObj.getFileName$())){
return false;
}}if(this.getLineNumber$()!=castObj.getLineNumber$()){
return false;
}return true;
});
m$(C$,"getClassName$",
function(){
return(this.declaringClass==null)?"<unknown class>":this.declaringClass;
});
m$(C$,"getFileName$",
function(){
return this.fileName;
});
m$(C$,"getLineNumber$",
function(){
return this.lineNumber;
});
m$(C$,"getMethodName$",
function(){
return(this.methodName==null)?"<unknown method>":this.methodName;
});
m$(C$,"hashCode$",
function(){
if(this.methodName==null){
return 0;
}return this.methodName.hashCode$()^this.declaringClass.hashCode();
});
m$(C$,"isNativeMethod$",
function(){
return this.lineNumber==-2;
});
m$(C$,"toString",
function(){
var s = this.getClassName$() + "." + this.getMethodName$();
if(this.isNativeMethod$()){
 s += "(Native Method)";
}else{
var fName=this.getFileName$();
if(fName==null){
 //s += "(Unknown Source)";
}else{
var lineNum=this.getLineNumber$();
s += '(' + fName;
if(lineNum>=0){
 s += ':' + lineNum;
}
 s += ')';
}}return s;
});


TypeError.prototype.getMessage$ || (
		
 ReferenceError.prototype.getMessage$ = TypeError.prototype.getMessage$ 
		= ReferenceError.prototype.getMessage$ = TypeError.prototype.getLocalizedMessage$ 
			= function(){ return (this.stack ? this.stack : this.message || this.toString()) + (this.getStackTrace ? this.getStackTrace$() : Clazz._getStackTrace())});

TypeError.prototype.getStackTrace$ = ReferenceError.prototype.getStackTrace$ = function() { return Clazz._getStackTrace() }
TypeError.prototype.printStackTrace$ = ReferenceError.prototype.printStackTrace$ = function() { printStackTrace(this,System.err) }
ReferenceError.prototype.printStackTrace$java_io_PrintStream = TypeError.prototype.printStackTrace$java_io_PrintStream = function(stream){stream.println$S(this + "\n" + this.stack);};
ReferenceError.prototype.printStackTrace$java_io_PrintWriter = TypeError.prototype.printStackTrace$java_io_PrintWriter = function(printer){printer.println$S(this + "\n" + this.stack);};

Clazz.Error = Error;

var declareType = function(prefix, name, clazzSuper, interfacez) {
  var cl = Clazz.newClass(prefix, name, null, clazzSuper, interfacez);
  if (clazzSuper)
    setSuperclass(cl, clazzSuper);
  return cl;
};

// at least allow Error() by itself to work as before
Clazz._Error || (Clazz._Error = Error);
// setSuperclass(Clazz._Error, Throwable);

var setEx = function(C$) {
 C$.$clinit$ = 1;
 m$(C$, "c$", function() { C$.superclazz.c$.apply(this, []);}, 1);
 m$(C$, "c$$S", function(detailMessage){C$.superclazz.c$$S.apply(this,[detailMessage]);},1);
 m$(C$, "c$$Throwable", function(exception){C$.superclazz.c$$Throwable.apply(this, arguments);}, 1);
 m$(C$, "c$$S$Throwable", function(detailMessage,exception){C$.superclazz.c$$S$Throwable.apply(this, arguments);
}, 1);

 return C$;
}

/*
;(function() {
var C$ = Clazz.newClass(java.lang, "Error", function (){
return Clazz._Error();
}, Throwable);
setEx(C$);
})();
*/
var newEx = function(prefix, name, clazzSuper) {
  return setEx(declareType(prefix, name, clazzSuper));
}

newEx(java.lang,"Exception",Throwable);
newEx(java.lang,"Error",Throwable);

newEx(java.lang,"RuntimeException",Exception);
newEx(java.lang,"IllegalArgumentException",RuntimeException);
newEx(java.lang,"LinkageError",Error);
newEx(java.lang,"VirtualMachineError",Error);
newEx(java.lang,"IncompatibleClassChangeError",LinkageError);

newEx(java.lang,"AbstractMethodError",IncompatibleClassChangeError);
newEx(java.lang,"ArithmeticException",RuntimeException);
newEx(java.lang,"ArrayStoreException",RuntimeException);
newEx(java.lang,"ClassCircularityError",LinkageError);
newEx(java.lang,"ClassFormatError",LinkageError);
newEx(java.lang,"CloneNotSupportedException",Exception);
newEx(java.lang,"ReflectiveOperationException",Exception);
newEx(java.lang,"IllegalAccessError",IncompatibleClassChangeError);
newEx(java.lang,"IllegalAccessException",ReflectiveOperationException);
newEx(java.lang,"IllegalMonitorStateException",RuntimeException);
newEx(java.lang,"IllegalStateException",RuntimeException);
newEx(java.lang,"IllegalThreadStateException",IllegalArgumentException);
newEx(java.lang,"IndexOutOfBoundsException",RuntimeException);
newEx(java.lang,"InstantiationError",IncompatibleClassChangeError);
newEx(java.lang,"InstantiationException",ReflectiveOperationException);
newEx(java.lang,"InternalError",VirtualMachineError);
newEx(java.lang,"InterruptedException",Exception);
newEx(java.lang,"NegativeArraySizeException",RuntimeException);
newEx(java.lang,"NoClassDefFoundError",LinkageError);
newEx(java.lang,"NoSuchFieldError",IncompatibleClassChangeError);
newEx(java.lang,"NoSuchFieldException",ReflectiveOperationException);
newEx(java.lang,"NoSuchMethodException",ReflectiveOperationException);
newEx(java.lang,"NoSuchMethodError",IncompatibleClassChangeError);
newEx(java.lang,"NullPointerException",RuntimeException);

;(function(){

var C$=Clazz.newClass(java.lang, "NumberFormatException", null, 'IllegalArgumentException');

C$.$clinit$=2;

Clazz.newMeth(C$, '$init$', function () {
},1);

Clazz.newMeth(C$, 'c$', function () {
;C$.superclazz.c$.apply(this,[]);C$.$init$.apply(this);
}, 1);

Clazz.newMeth(C$, 'c$$S', function (s) {
;C$.superclazz.c$$S.apply(this,[s]);C$.$init$.apply(this);
}, 1);

Clazz.newMeth(C$, 'forInputString$S', function (s) {
return Clazz.new_(C$.c$$S,["For input string: \"" + s + "\"" ]);
}, 1);
})();

newEx(java.lang,"OutOfMemoryError",VirtualMachineError);
newEx(java.lang,"SecurityException",RuntimeException);
newEx(java.lang,"StackOverflowError",VirtualMachineError);
newEx(java.lang,"ThreadDeath",Error);
newEx(java.lang,"UnknownError",VirtualMachineError);
newEx(java.lang,"UnsatisfiedLinkError",LinkageError);
newEx(java.lang,"UnsupportedClassVersionError",ClassFormatError);
newEx(java.lang,"UnsupportedOperationException",RuntimeException);
newEx(java.lang,"VerifyError",LinkageError);

newEx(java.lang,"ClassCastException",RuntimeException);

;(function() {
var C$=Clazz.newClass(java.lang,"ClassNotFoundException",function(){this.ex=null;},Exception);
m$(C$, "c$$S$Throwable", function(detailMessage,exception){
C$.superclazz.c$$S$Throwable.apply(this, arguments);
this.ex=exception;
}, 1);
m$(C$,"getException$",
function(){
return this.ex;
});
m$(C$,"getCause$",
function(){
return this.ex;
});
})();

;(function() {
var C$=newEx(java.lang,"StringIndexOutOfBoundsException",IndexOutOfBoundsException);
m$(C$, "c$$I", function(index){
C$.superclazz.c$$S.apply(this,["String index out of range: "+index]);
}, 1);
})();

;(function() {
var C$=Clazz.newClass(java.lang.reflect,"InvocationTargetException",function(){this.target=null;},ReflectiveOperationException);
C$.$clinit$ = 2;
m$(C$, "c$$Throwable", function(exception){
C$.superclazz.c$$Throwable.apply(this, arguments);
this.target=exception;
}, 1);
m$(C$, "c$$Throwable$S", function(exception,detailMessage){
C$.superclazz.c$$S$Throwable.apply(this,[detailMessage,exception]);
this.target=exception;
}, 1);
m$(C$,"getTargetException$",
function(){
return this.target;
});
m$(C$,"getCause$",
function(){
return this.target;
});
})()

;(function(){
var C$=Clazz.newClass(java.lang.reflect,"UndeclaredThrowableException",function(){this.undeclaredThrowable=null;},RuntimeException);
C$.$clinit$ = 2;
m$(C$, "c$$Throwable", function(exception){
Clazz.super_(C$, this);
C$.superclazz.c$$Throwable.apply(this, arguments);
this.undeclaredThrowable=exception;
this.initCause(exception);
},1);
m$(C$, "c$$Throwable$S", function(exception,detailMessage){
C$.superclazz.c$$S.apply(this,[detailMessage]);
this.undeclaredThrowable=exception;
this.initCause(exception);
},1);
m$(C$,"getUndeclaredThrowable$",
function(){
return this.undeclaredThrowable;
});
m$(C$,"getCause$",
function(){
return this.undeclaredThrowable;
});
})();

newEx(java.io,"IOException",Exception);
newEx(java.io,"CharConversionException",java.io.IOException);
newEx(java.io,"EOFException",java.io.IOException);
newEx(java.io,"FileNotFoundException",java.io.IOException);
newEx(java.io,"ObjectStreamException",java.io.IOException);
newEx(java.io,"SyncFailedException",java.io.IOException);
newEx(java.io,"UnsupportedEncodingException",java.io.IOException);
newEx(java.io,"UTFDataFormatException",java.io.IOException);

newEx(java.io,"InvalidObjectException",java.io.ObjectStreamException);
newEx(java.io,"NotActiveException",java.io.ObjectStreamException);
newEx(java.io,"NotSerializableException",java.io.ObjectStreamException);
newEx(java.io,"StreamCorruptedException",java.io.ObjectStreamException);

;(function() {
var C$=Clazz.newClass(java.io,"InterruptedIOException",function(){
this.bytesTransferred=0;
},java.io.IOException);
})();


;(function() {
var C$=Clazz.newClass(java.io,"InvalidClassException",function(){
this.classname=null;
},java.io.ObjectStreamException);

m$(C$, "c$$S$S", function(className,detailMessage){
C$.superclazz.c$$S.apply(this,[detailMessage]);
this.classname=className;
},1);

m$(C$,"getMessage$",
function(){
var msg=C$.superclazz.getMessage$.apply(this, []);
if(this.classname!=null){
msg=this.classname+';' + ' '+msg;
}return msg;
});
})();


;(function(){
var C$=Clazz.newClass(java.io,"OptionalDataException",function(){
this.eof=false;
this.length=0;
},java.io.ObjectStreamException);
})();

;(function() {
var C$=Clazz.newClass(java.io,"WriteAbortedException",function(){
this.detail=null;
},java.io.ObjectStreamException);

m$(C$, "c$$S$Throwable", function(detailMessage, rootCause){
C$.superclazz.c$$S.apply(this,[detailMessage]);
this.detail=rootCause;
this.initCause(rootCause);
}, 1);

m$(C$,"getMessage$",
function(){
var msg=C$.superclazz.getMessage.apply(this);
return (this.detail ? msg + "; "+this.detail.toString() : msg);
});
m$(C$,"getCause$",
function(){
return this.detail;
});
})();

newEx(java.util,"EmptyStackException",RuntimeException);
newEx(java.util,"NoSuchElementException",RuntimeException);
newEx(java.util,"TooManyListenersException",Exception);


;(function(){
var C$=newEx(java.util,"ConcurrentModificationException",RuntimeException);
m$(C$, "c$", function(detailMessage, rootCause){
Clazz.super_(C$, this);
}, 1);
})();

;(function(){
var C$=Clazz.newClass(java.util,"MissingResourceException",function(){
this.className=null;
this.key=null;
},RuntimeException);
C$.$clinit$ = 1;
m$(C$, "c$$S$S$S", function(detailMessage,className,resourceName){
Clazz.super_(C$, this);
C$.superclazz.c$$S.apply(this,[detailMessage]);
this.className=className;
this.key=resourceName;
}, 1);
m$(C$,"getClassName$",
function(){
return this.className;
});
m$(C$,"getKey$",
function(){
return this.key;
});
})();

declareType(java.lang,"Void");
setJ2STypeclass(java.lang.Void, "void", "V");
// java.lang.Void.TYPE=java.lang.Void;
// java.lang.V

Clazz.newInterface(java.lang.reflect,"GenericDeclaration");

Clazz.newInterface(java.lang.reflect,"InvocationHandler");

C$=Clazz.newInterface(java.lang.reflect,"Member");

C$=declareType(java.lang.reflect,"Modifier");
m$(C$, "c$", function(){}, 1);

m$(C$,"isAbstract$I",
function(modifiers){
return((modifiers&1024)!=0);
}, 1);
m$(C$,"isFinal$I",
function(modifiers){
return((modifiers&16)!=0);
}, 1);
m$(C$,"isInterface$I",
function(modifiers){
return((modifiers&512)!=0);
}, 1);
m$(C$,"isNative$I",
function(modifiers){
return((modifiers&256)!=0);
}, 1);
m$(C$,"isPrivate$I",
function(modifiers){
return((modifiers&2)!=0);
}, 1);
m$(C$,"isProtected$I",
function(modifiers){
return((modifiers&4)!=0);
}, 1);
m$(C$,"isPublic$I",
function(modifiers){
return((modifiers&1)!=0);
}, 1);
m$(C$,"isStatic$I",
function(modifiers){
return((modifiers&8)!=0);
}, 1);
m$(C$,"isStrict$I",
function(modifiers){
return((modifiers&2048)!=0);
}, 1);
m$(C$,"isSynchronized$I",
function(modifiers){
return((modifiers&32)!=0);
}, 1);
m$(C$,"isTransient$I",
function(modifiers){
return((modifiers&128)!=0);
}, 1);
m$(C$,"isVolatile$I",
function(modifiers){
return((modifiers&64)!=0);
}, 1);
m$(C$,"toString",
function(modifiers){
var sb=new Array(0);
if(java.lang.reflect.Modifier.isPublic(modifiers))sb[sb.length]="public";
if(java.lang.reflect.Modifier.isProtected(modifiers))sb[sb.length]="protected";
if(java.lang.reflect.Modifier.isPrivate(modifiers))sb[sb.length]="private";
if(java.lang.reflect.Modifier.isAbstract(modifiers))sb[sb.length]="abstract";
if(java.lang.reflect.Modifier.isStatic(modifiers))sb[sb.length]="static";
if(java.lang.reflect.Modifier.isFinal(modifiers))sb[sb.length]="final";
if(java.lang.reflect.Modifier.isTransient(modifiers))sb[sb.length]="transient";
if(java.lang.reflect.Modifier.isVolatile(modifiers))sb[sb.length]="volatile";
if(java.lang.reflect.Modifier.isSynchronized(modifiers))sb[sb.length]="synchronized";
if(java.lang.reflect.Modifier.isNative(modifiers))sb[sb.length]="native";
if(java.lang.reflect.Modifier.isStrict(modifiers))sb[sb.length]="strictfp";
if(java.lang.reflect.Modifier.isInterface(modifiers))sb[sb.length]="interface";
if(sb.length>0){
return sb.join(" ");
}return"";
}, 1);

var newMethodNotFoundException = function (clazz, method) {
  var message = "Method " + getClassName(clazz, true) + (method ? "." 
          + method : "") + " was not found";
  System.out.println(message);
  console.log(message);
  throw Clazz.new_(java.lang.NoSuchMethodException.c$$S, [message]);        
};

// if (needPackage("core"))
  // _Loader.loadPackage("core");


// Clazz._Loader.loadZJar(Clazz._Loader.getJ2SLibBase() +
// "core/coreswingjs.z.js", "swingjs.JSUtil");

  // if (!J2S._isAsync) {
if (!J2S._loadcore || J2S._coreFiles.length == 0) {
	if (J2S._verbose)System.out.println("Clazz: No core files to load -- check Info.core"); 
} else {  
  J2S.onClazzLoaded && J2S.onClazzLoaded(1, "Clazz loaded; loading J2S._coreFiles " + J2S._coreFiles.length);
  for (var i = 0; i < J2S._coreFiles.length; i++) {
	Clazz.loadScript(J2S._coreFiles[i]);
  }
  J2S.onClazzLoaded && J2S.onClazzLoaded(2, "Clazz loaded; core files loaded");
}

} // LoadClazz
})(J2S, window, document); 
// SwingJSApplet.js

// BH 8/1/2018 $-qualified Java methods
// generic SwingJS Applet
// BH 3/14/2018 8:42:33 PM adds applet._window for JSObject
// BH 12/18/2016 8:09:56 AM added SwingJS.Loaded and SwingJS.isLoaded
// BH 7/24/2015 9:09:39 AM allows setting Info.resourcePath
// BH 4/28/2015 10:15:32 PM adds getAppletHtml 
// BH 4/2/2015 5:17:44 PM  adds SwingJS.getJavaResource(path)

// BH 3/27/2015 6:34:49 AM  just a shell

if(typeof(jQuery)=="undefined") alert ("Note -- jQuery is required for SwingJS, but it's not defined.")

if (typeof(SwingJS) == "undefined") {

  SwingJS = {eventID:0};

(function (SwingJS, $, J2S) {

	SwingJS.getApplet = function(id, Info, checkOnly) {
		if (arguments.length == 1 && typeof id == "object") {
			// allow for getApplet(Info)
			Info = id;
			id = null;
		}
		return SwingJS._Applet._get(id, Info, checkOnly);
	}

  	// optional Info here	
	SwingJS.getAppletHtml = function(applet, Info) {
		if (arguments.length == 1 && typeof applet == "object" && !applet._code) {
			// allow for getApplet(Info)
			Info = applet;
			applet = null;
		}
		if (Info) {
			var d = SwingJS._document;
			SwingJS._document = null;
			applet = SwingJS.getApplet(applet, Info);
			SwingJS._document = d;
		}  
		return applet._code;
	}

	SwingJS._Applet = function(id, Info, checkOnly){
		window[id] = this;
		this._appletType = "SwingJS._Applet" + (Info.isSigned ? " (signed)" : "");
		this._isJava = true;
		this._availableParams = null; // all allowed
    this._window = window;
		if (checkOnly)
			return this;
		this._isSigned = Info.isSigned;
		this._readyFunction = Info.readyFunction;
		this._ready = false;
		this._isJava = true; 
		this._isInfoVisible = false;
		this._applet = null;
		this._memoryLimit = Info.memoryLimit || 512;
		this._canScript = function(script) {return true;};
		this._savedOrientations = [];
		this._initialize = function(jarPath, jarFile) {
			var doReport = false;
			SwingJS._jarFile && (jarFile = SwingJS._jarFile);
			if(this._jarFile) {
				var f = this._jarFile;
				if(f.indexOf("/") >= 0) {
					alert ("This web page URL is requesting that the applet used be " + f + ". This is a possible security risk, particularly if the applet is signed, because signed applets can read and write files on your local machine or network.");
					var ok = prompt("Do you want to use applet " + f + "? ", "yes or no")
					if(ok == "yes") {
						jarPath = f.substring(0, f.lastIndexOf("/"));
						jarFile = f.substring(f.lastIndexOf("/") + 1);
					} else {
						doReport = true;
					}
				} else {
					jarFile = f;
				}
				this_isSigned = Info.isSigned = (jarFile.indexOf("Signed") >= 0);
			}
 			this._jarPath = Info.jarPath = jarPath || ".";
			this._jarFile = Info.jarFile = jarFile;
			if (doReport)
				alert ("The web page URL was ignored. Continuing using " + this._jarFile + ' in directory "' + this._jarPath + '"');
			// could do something like this: J2S.controls == undefined || J2S.controls._onloadResetForms();		
		}		
		this._create(id, Info);
		return this;
	}

	;(function(Applet, proto) {
  
	Applet._get = function(id, Info, checkOnly) {

		checkOnly || (checkOnly = false);
		Info || (Info = {});
		var DefaultInfo = {
      code: null,//"swingjs.test.TanSugd3S",
      uncompressed: true,
			//color: "#FFFFFF", // applet object background color
			width: 300,
			height: 300,
			serverURL: "http://your.server.here/jsmol.php",
  	  console: null,  // div for where the JavaScript console will be.
			readyFunction: null,
			use: "HTML5",//other options include JAVA
			jarPath: "java",
			jarFile: "[code].jar",
			j2sPath: "j2s",
			spinnerImage: "core/Spinner.gif",
			disableJ2SLoadMonitor: false,
			disableInitialConsole: false,
			debug: false
		};	 
		id || (id = Info.name) || (id = "j2sApplet" + J2S._defaultID++);
    
		J2S._addDefaultInfo(Info, DefaultInfo);
		
		
    Info.jarFile && Info.code && Info.jarFile.replace(/\[code\]/,Info.code);
		J2S._debugAlert = Info.debug;
		Info.serverURL && (J2S._serverUrl = Info.serverURL);

		var javaAllowed = false;
		var applet = null;
		var List = Info.use.toUpperCase().split("#")[0].split(" ");
		for (var i = 0; i < List.length; i++) {
			switch (List[i]) {
			case "JAVA":
				javaAllowed = true;
				if (J2S.featureDetection.supportsJava())
					applet = new Applet(id, Info, checkOnly);
				break;
			case "HTML5":               
  			if (J2S.featureDetection.allowHTML5){
				  applet = Applet._getCanvas(id, Info, checkOnly);
        } else {
          List.push("JAVA");
        }
				break;
			}
			if (applet != null)
				break;		  
		}
		if (applet == null) {
			if (checkOnly || !javaAllowed)
				applet = {_appletType : "none" };
			else if (javaAllowed)
 		  	applet = new Applet(id, Info);
		}

		// keyed to both its string id and itself
		return (checkOnly ? applet : J2S._registerApplet(id, applet));  
	}

	Applet._getCanvas = function(id, Info, checkOnly) {
    Info._isLayered = true;
    Info._isSwing = true;
    Info._platform = "";
		J2S._Canvas2D.prototype = J2S._jsSetPrototype(new Applet(id, Info, true));
		return new J2S._Canvas2D(id, Info, Info.code, checkOnly);
	};

	/*  AngelH, mar2007:
		By (re)setting these variables in the webpage before calling J2S.getApplet(),
		a custom message can be provided (e.g. localized for user's language) when no Java is installed.
	*/
	Applet._noJavaMsg =
			"Either you do not have Java applets enabled in your web<br />browser or your browser is blocking this applet.<br />\
			Check the warning message from your browser and/or enable Java applets in<br />\
			your web browser preferences, or install the Java Runtime Environment from <a href='http://www.java.com'>www.java.com</a>";

	Applet._setCommonMethods = function(p) {
		p._showInfo = proto._showInfo;	
///		p._search = proto._search;
		p._getName = proto._getName;
		p.readyCallback = proto.readyCallback;
	}

	Applet._createApplet = function(applet, Info, params) {
		applet._initialize(Info.jarPath, Info.jarFile);
		var jarFile = applet._jarFile;
		var jnlp = ""
		if (J2S._isFile) {
			jarFile = jarFile.replace(/0\.jar/,".jar");
		}
		// size is set to 100% of containers' size, but only if resizable. 
		// Note that resizability in MSIE requires: 
		// <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
		var w = (applet._containerWidth.indexOf("px") >= 0 ? applet._containerWidth : "100%");
		var h = (applet._containerHeight.indexOf("px") >= 0 ? applet._containerHeight : "100%");
		var widthAndHeight = " style=\"width:" + w + ";height:" + h + "\" ";
		var attributes = "name='" + applet._id + "_object' id='" + applet._id + "_object' " + "\n"
				+ widthAndHeight + jnlp + "\n"
		params.codebase = applet._jarPath;
		params.codePath = params.codebase + "/";
		if (params.codePath.indexOf("://") < 0) {
			var base = document.location.href.split("#")[0].split("?")[0].split("/");
			base[base.length - 1] = params.codePath;
			params.codePath = base.join("/");
		}
		params.archive = jarFile;
		params.mayscript = 'true';
		params.java_arguments = "-Xmx" + Math.round(Info.memoryLimit || applet._memoryLimit) + "m";
		params.permissions = (applet._isSigned ? "all-permissions" : "sandbox");
		params.documentLocation = document.location.href;
		params.documentBase = document.location.href.split("#")[0].split("?")[0];

		params.jarPath = Info.jarPath;
		J2S._syncedApplets.length && (params.synccallback = "J2S._mySyncCallback");
		applet._startupScript && (params.script = applet._startupScript);
		var t = "\n"; 
 		for (var i in params)
			if(params[i])
		 		t += "  <param name='"+i+"' value='"+params[i]+"' />\n";
		if (J2S.featureDetection.useIEObject || J2S.featureDetection.useHtml4Object) {
			t = "<object " + attributes
				+ (J2S.featureDetection.useIEObject ? 
					 " classid='clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' codebase='http://java.sun.com/update/1.6.0/jinstall-6u22-windows-i586.cab'>"
				 : " type='application/x-java-applet'>")
				 + t + "<p style='background-color:yellow;" + widthAndHeight.split('"')[1] 
				+ ";text-align:center;vertical-align:middle;'>\n" + Applet._noJavaMsg + "</p></object>\n";
		} else { // use applet tag
			t = "<applet " + attributes
				+ " code='" + params.code + "' codebase='" + applet._jarPath + "' archive='" + jarFile + "' mayscript='true'>\n"
				+ t + "<table bgcolor='yellow'><tr><td align='center' valign='middle' " + widthAndHeight + ">\n"
				+ Applet._noJavaMsg + "</td></tr></table></applet>\n";
		}
		if (applet._deferApplet)
			applet._javaCode = t, t="";
		t = J2S._getWrapper(applet, true) + t + J2S._getWrapper(applet, false) 
			+ (Info.addSelectionOptions ? J2S._getGrabberOptions(applet) : "");
		if (J2S._debugAlert)
			alert (t);
		applet._code = J2S._documentWrite(t);
	}

	proto._newApplet = function(viewerOptions) {
		this._viewerOptions = viewerOptions;
    // for now assigning this._applet here instead of in readyCallback
    Clazz.loadClass("swingjs.JSAppletViewer");
		this._appletPanel = Clazz.new_(swingjs.JSAppletViewer.c$$java_util_Hashtable, [viewerOptions]);
    this._appletPanel.start$();
	}
	
	proto._addCoreFiles = function() {
		if (this.__Info.core != "NONE" && this.__Info.core != "none" && !J2S._debugCode)
			J2S._addCoreFile((this.__Info.core || "swingjs"), this._j2sPath, this.__Info.preloadCore);
//		if (J2S._debugCode) {
//		// no min package for that
//			J2S._addExec([this, null, "swingjs.JSAppletViewer", "load " + this.__Info.code]);
//      
//		}
  }
  
	proto._create = function(id, Info){
		J2S._setObject(this, id, Info);
		var params = {
			syncId: J2S._syncId,
			progressbar: "true",                      
			progresscolor: "blue",
			boxbgcolor: this._color || "black",
			boxfgcolor: "white",
			boxmessage: "Downloading Applet ...",
			//script: (this._color ? "background \"" + this._color +"\"": ""),
			code: Info.appletClass + ".class"
		};

		J2S._setAppletParams(this._availableParams, params, Info);
		function sterilizeInline(model) {
			model = model.replace(/\r|\n|\r\n/g, (model.indexOf("|") >= 0 ? "\\/n" : "|")).replace(/'/g, "&#39;");
			if(J2S._debugAlert)
				alert ("inline model:\n" + model);
			return model;
		}

		params.loadInline = (Info.inlineModel ? sterilizeInline(Info.inlineModel) : "");
		params.appletReadyCallback = "J2S.readyCallback";
		if (J2S._syncedApplets.length)
			params.synccallback = "J2S._mySyncCallback";
		params.java_arguments = "-Xmx" + Math.round(Info.memoryLimit || this._memoryLimit) + "m";

		this._initialize(Info.jarPath, Info.jarFile);
		Applet._createApplet(this, Info, params);
	}


	proto._restoreState = function(clazzName, state) {
   // applet-dependent
	}

	proto.readyCallback = function(id, fullid, isReady) {
		if (!isReady)
			return; // ignore -- page is closing
		J2S._setDestroy(this);
		this._ready = true;
		this._showInfo(true);
		this._showInfo(false);
		J2S.Cache.setDragDrop(this);
		this._readyFunction && this._readyFunction(this);
		J2S._setReady(this);
		var app = this._2dapplet;
		if (app && app._isEmbedded && app._ready && app.__Info.visible)
			this._show2d(true);
	}

	proto._showInfo = function(tf) {
    if(this._isJNLP)return;
		if(tf && this._2dapplet)
			this._2dapplet._show(false);
		J2S.$html(J2S.$(this, "infoheaderspan"), this._infoHeader);
		if (this._info)
			J2S.$html(J2S.$(this, "infodiv"), this._info);
		if ((!this._isInfoVisible) == (!tf))
			return;
		this._isInfoVisible = tf;
		// 1px does not work for MSIE
		if (this._isJava) {
			var x = (tf ? 2 : "100%");
			J2S.$setSize(J2S.$(this, "appletdiv"), x, x);
		}
		J2S.$setVisible(J2S.$(this, "infotablediv"), tf);
		J2S.$setVisible(J2S.$(this, "infoheaderdiv"), tf);
		this._show(!tf);
	}

	proto._show = function(tf) {
		var x = (!tf ? 2 : "100%");
		J2S.$setSize(J2S.$(this, "object"), x, x);
		if (!this._isJava)
			J2S.$setVisible(J2S.$(this, "appletdiv"), tf);
	}

	proto._clearConsole = function () {
			if (this._console == this._id + "_infodiv")
				this.info = "";
			if (!self.Clazz)return;
			J2S._setConsoleDiv(this._console);
			Clazz.Console.clear();
		}

	proto._resizeApplet = function(size) {
		// See _jmolGetAppletSize() for the formats accepted as size [same used by jmolApplet()]
		//  Special case: an empty value for width or height is accepted, meaning no change in that dimension.

		/*
		 * private functions
		 */
		function _getAppletSize(size, units) {
			/* Accepts single number, 2-value array, or object with width and height as mroperties, each one can be one of:
			 percent (text string ending %), decimal 0 to 1 (percent/100), number, or text string (interpreted as nr.)
			 [width, height] array of strings is returned, with units added if specified.
			 Percent is relative to container div or element (which should have explicitly set size).
			 */
			var width, height;
			if(( typeof size) == "object" && size != null) {
				width = size[0]||size.width;
				height = size[1]||size.height;
			} else {
				width = height = size;
			}
			return [J2S.fixDim(width, units), J2S.fixDim(height, units)];
		}
		var sz = _getAppletSize(size, "px");
		var d = J2S._getElement(this, "appletinfotablediv");
		d.style.width = sz[0];
		d.style.height = sz[1];
		this._containerWidth = sz[0];
		this._containerHeight = sz[1];
		if (this._is2D)
			J2S.repaint(this, true);
	}

	proto._cover = function (doCover) {
    // from using getAppletHtml()
		this._newCanvas(false);
		this._showInfo(false);
		this._init();
	};


  
})(SwingJS._Applet, SwingJS._Applet.prototype);

})(SwingJS, jQuery, J2S);

} // SwingJS undefined
