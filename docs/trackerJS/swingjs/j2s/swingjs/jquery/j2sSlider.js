/**
 *  NOTE: If you change this file, copy its contents into 
 *  its spot at the end of JQueryUI.java. Or, if you are testing
 *  work here and set JQueryUI.loadResourceInternal = false only while testing,
 *  then do that paste.
 *  
 */

// based on  jQuery UI - v1.9.2 - 2015-05-28


;(function($) {
	
J2S.__makeSlider = function() {
	// run once; set to NOP
  J2S.__makeSlider = function(){};	

		$('head').append('<style>\
	.ui-j2sslider-at-bottom { border-bottom-color:red }\
	.ui-j2sslider-at-left { border-left-color:red }\
	.ui-j2sslider-at-right { border-right-color:red }\
	.ui-j2sslider-at-top { border-top-color:red }\
	.ui-j2sslider-tick-mark-horiz { display:inline-block; height:1px; background: black; border:1px; width:5px; position:absolute; left:14px; }\
	.ui-j2sslider-tick-mark-vert { display:inline-block; width:1px; background: black; border:1px; height:5px; position:absolute; top:12px; }\
	.ui-j2sscrollbar-horizontal { color:black;border-style:none solid none solid; border-width: 0px 5px; height: .3em; top: 40%;margin:0px 2px}\
	.ui-j2sscrollbar-vertical { color:black;border-style:solid none solid none; border-width: 5px 0px; width: .3em; left: 40%; margin:2px 0px }\
	.ui-j2sslider-horizontal .ui-j2sslider-handle { margin-left: -.4em; border:1px solid blue; box-sizing:border-box;}\
	.ui-j2sslider-horizontal .ui-j2sslider-range-max { right: 0; }\
	.ui-j2sslider-horizontal .ui-j2sslider-range-min { left: 0; }\
	.ui-j2sslider-horizontal .ui-j2sslider-range { top: 0.1em; height: 30%; }\
	.ui-j2sslider-horizontal { height: .3em; top: 40%;margin:0px 9px}\
	.ui-j2sslider-vertical .ui-j2sslider-handle { margin-left: 0; margin-bottom: -.3em;border:1px solid blue; box-sizing:border-box; }\
	.ui-j2sslider-vertical .ui-j2sslider-range-max { top: 0; }\
	.ui-j2sslider-vertical .ui-j2sslider-range-min { bottom: 0; }\
	.ui-j2sslider-vertical .ui-j2sslider-range { left: 0.1em; width: 30%; }\
	.ui-j2sslider-vertical { width: .3em; left: 40%; margin:10px 0px }\
	.ui-j2sslider .ui-j2sslider-handle { position: absolute; width: 0.8em; height: 0.8em; cursor: default; }\
	.ui-j2sslider .ui-j2sslider-range { position: absolute; font-size: 0.3em; display: block; border: 0; background-position: 0 0; }\
	.ui-j2sslider { position: relative; text-align: left;}\
	.ui-state-disabled { cursor: default !important; }</style>');
		

		
// number of pages in a slider
// (how many times can you page 3/down to go through the whole range)
var numPages = 5;

var closestHandle, index, allowed, offset, dir;

var actionTimer, actionTimer2;
var actionDelay = 60, actionDelay0 = 200;
var startAction = function(me, dir) {
	if (actionTimer)
		return;
	me.jslider.ui.scrollByUnit$I(dir)
	actionTimer = setTimeout(function(){
		actionTimer = setInterval(function() {
			me.jslider.ui.scrollByUnit$I(dir)
		}, actionDelay);
	}, actionDelay0);
}

var startAction2 = function(me, dir, val) {
	me.jslider.ui.scrollDueToClickInTrack$I$I(dir, val);
	if (!me.isScrollBar)
		return;
	actionTimer2 = 
		setTimeout(function(){
			actionTimer2 = setInterval(function() {
				me.jslider.ui.scrollDueToClickInTrack$I$I(dir);
			}, actionDelay);
		}, actionDelay0);
}

var clearEnds = function(me) {
	var e = me.element;
	if (actionTimer)
		clearInterval(actionTimer);
	if (actionTimer2)
		clearInterval(actionTimer2);
	actionTimer = 0;
	actionTimer2 = 0;
	e.removeClass("ui-j2sslider-at-top");
	e.removeClass("ui-j2sslider-at-bottom");
	e.removeClass("ui-j2sslider-at-left");
	e.removeClass("ui-j2sslider-at-right");
}

var OBJ_WRAP = 0;
var OBJ_TRACK = 1;
var OBJ_HANDLE = 2;

var doMouseCapture = function(me, event, obj, isEndCheck) {

	var that = me, o = me.options;
	
	if (o.disabled ||
			(event.type == "mousemove" || event.type == "pointermove") && event.buttons == 0) {
		return false;
	}

	var position = me._getPosition(event);

	index = event.target.index;

	closestHandle = $(event.target);// handles[index];//$(
									// me );

	// workaround for bug #3736 (if both handles of
	// a range are at 0,
	// the first is always used as the one with
	// least distance,
	// and moving it is obviously prevented by
	// preventing negative ranges)
	if (o.range === true
			&& me.values(1) === o.min) {
		index += 1;
		closestHandle = $(me.handles[index]);
	}

	allowed = (obj == OBJ_HANDLE ? me._start(event, index) : true);
	if (allowed === false) {
		return false;
	}
	
	me._mouseSliding = true;

	me._handleIndex = index;

	if (obj == OBJ_HANDLE)
		closestHandle.addClass("ui-state-active").focus();

	offset = closestHandle.offset();
	var mouseOverHandle = (obj == OBJ_HANDLE) && $(event.target).parents()
			.andSelf().is(".ui-j2sslider-handle");
	me.closestHandle = closestHandle;
	me._clickOffset = (mouseOverHandle ? 
		{
		left : position.x
				- offset.left
				- (closestHandle.width() / 2 * o.scaleX)
				,
		top : position.y
				- offset.top
				- (closestHandle.height() / 2 * o.scaleY)
				- (parseInt(closestHandle
						.css("borderTopWidth"), 10) || 0)
				- (parseInt(closestHandle
						.css("borderBottomWidth"),
						10) || 0)
				+ (parseInt(closestHandle
						.css("marginTop"), 10) || 0)
		} : {
			left : 0,
			top : 0
		});
	var val = normValueFromMouse(me, position, obj);
	var pixmouse = getPixelMouse(me, position, false);
	
	var isAtEnd = !mouseOverHandle && (!me.isScrollBar ? 0 : 
		pixmouse < 5 ? -1 : pixmouse > length(me) + 5 ? 1 : 0);
	var dir = Math.signum(!isAtEnd ? val - me.jslider.getValue$() : isAtEnd);
	if (isAtEnd) {
		me.element.addClass(me.orientation === "horizontal" ? 
				(isAtEnd == 1 ? "ui-j2sslider-at-right" : "ui-j2sslider-at-left")
				: (isAtEnd == 1 ? "ui-j2sslider-at-bottom" : "ui-j2sslider-at-top"));
		startAction(me, dir);	
	} else {
		clearEnds(me);				
		if (isEndCheck) {
			return;
		}
//				if (!me.handles.hasClass("ui-state-hover")) {
			if (obj != OBJ_HANDLE) {
				startAction2(me, dir, val);
//					}
		}
	}
	me._animateOff = true;
	return true;
}

var normValueFromMouse = function(me, position, obj) {
	var pixelMouse = getPixelMouse(me, position, true);
	var fMouse = (pixelMouse / getPixelTotal(me));						
	if (fMouse > 1) {
		fMouse = 1;
	}
	if (fMouse < 0) {
		fMouse = 0;
	}
	if (me.orientation === "vertical") {
		fMouse = 1 - fMouse;
	}
	if (me.options.inverted) {
		fMouse = 1 - fMouse;
	}
	var valueTotal = me._valueMax()
			- me._valueMin();
	var valueMouse = me._valueMin() + fMouse * valueTotal;
	return me._trimAlignValue(valueMouse);
}

var getPixelMouse = function(me, position, offsetHandle) {
	var offset = me.element.offset();
	var p = (me.orientation === "horizontal" ?
			position.x
				- offset.left
				- (me._clickOffset ? me._clickOffset.left : 0)
		 : position.y
				- offset.top
				- (me._clickOffset ? me._clickOffset.top : 0));
	return p - (offsetHandle ? me.handleSize / 2 : 0);
}

var length = function(me) {
	return (me.orientation == "horizontal" ? width(me) : height(me))
}

var getPixelTotal = function(me) {
	return length(me) - me.visibleAdjust || 100;	
}

var postMouseEvent = function(me, xye, id) {
	// set target to the handle
	xye.ev.currentTarget
		&& (xye.ev.target = xye.ev.currentTarget);
	// pass event to JSlider in case there is a
	// mouse listener implemented for that
	// InputEvent.BUTTON1 +
	// InputEvent.BUTTON1_DOWN_MASK;
	// same call here as in j2sApplet
	me.jslider.getFrameViewer$()
		.processMouseEvent$I$I$I$I$J$O$I(
				id, xye.x, xye.y, 1040,
				System.currentTimeMillis$(),
				xye.ev);
}

var width = function(me) {
	var w = Math.max(0, me.element.width() || me.element.parent().width() - me.marginX || 0);
	return w;
}

var height = function(me) {
	return Math.max(0, me.element.height() || me.element.parent().height() - me.marginY || 0);
}


$.widget(
	"ui.j2sslider",
	$.ui.mouse,
	{
		version : "1.9.2",
		widgetEventPrefix : "slide",

		options : {
			jslider : null,
			animate : false,
			distance : 0,
			scaleX : 1, // diamond cursor scaling X
			scaleY : 1, // diamand cursor scaling Y
			max : 100,
			min : 0,
			isScrollBar : false,
			orientation : "horizontal",
			range : false,
			step : 1,
			value : 0,
			inverted : false,
			values : null
		},

		_create : function() {
			var handleCount, 
				o = this.options, 
				existingHandles = this.element.find(".ui-j2sslider-handle").addClass("ui-state-default"),// ui-corner-all"), 
				handle = "<a class='ui-j2sslider-handle ui-state-default' href='#'></a>", // was ui-corner-all 
				handles = [];
			this.jslider || (this.jslider = o.jslider);
			this._keySliding = false;
			this._mouseSliding = false;
			this._animateOff = true;
			this._handleIndex = null;
			this._detectOrientation();
			this._mouseInit();
			this.isScrollBar = o.isScrollBar;
			this.handleSize = 0; // scrollbar only
			this.visibleAmount = 0;
			this.visibleAdjust = 0;
			this.visibleFraction = 0;
			this.handleFraction = 0;
			this.marginX = (o.isScrollBar ? 0 : 19); // from CSS - margin * 2 + border
			this.marginY = (o.isScrollBar ? 0 : 0);
			this.element
					.addClass("ui-j2sslider"
							+ " ui-j2sslider-"
							+ this.orientation
							+ " ui-widget"
							+ " ui-widget-content"
							+ " ui-corner-all"
							+ (o.disabled ? " ui-j2sslider-disabled ui-disabled"
									: ""));
			this.range = $([]);

			if (o.range) {
				if (o.range === true) {
					if (!o.values) {
						o.values = [ this._valueMin(),
								this._valueMin() ];
					}
					if (o.values.length
							&& o.values.length !== 2) {
						o.values = [ o.values[0],
								o.values[0] ];
					}
				}

				this.range = $("<div></div>")
					.appendTo(this.element)
					.addClass(
							"ui-j2sslider-range"
								+
								// note: this isn't
								// the most
								// fittingly
								// semantic
								// framework class
								// for this element,
								// but worked best
								// visually with a
								// variety of themes
								" ui-widget-header"
								+ ((o.range === "min" || o.range === "max") ? " ui-j2sslider-range-" + o.range : "")
					);
			}

			var me = this;

			var fDown = function(xye, id) {
				doMouseCapture(me, xye.ev, OBJ_HANDLE, false);
				postMouseEvent(me, xye, id);
			};

			var fDownTrack = function(event, id) {
				doMouseCapture(me, event, OBJ_TRACK, false);
				me._mouseSliding = false;
			};

			var fUpTrack = function(event, id) {
				//me._stop(event, me._handleIndex);
				me._change(event, me._handleIndex);
				clearEnds(me);
			};

			var fDownWrap = function(event, id) {
				doMouseCapture(me, event, OBJ_WRAP, false);
				me._mouseSliding = false;
			};

			var fDrag = function(xye, id) {
				if (id != 506 || me.options.disabled)
					return;
				var event = xye.ev;
				var position = me._getPosition(event);
				var normValue = normValueFromMouse(me, position, OBJ_HANDLE);
				me._slide(event, me._handleIndex, normValue);
				postMouseEvent(me, xye, id);
			};

			var fUp = function(xye, id) {
				if (me.options.disabled)
					return;
				var event = xye.ev;
				me.handles.removeClass("ui-state-active");
				me._mouseSliding = false;
				me._stop(event, me._handleIndex);
				me._change(event, me._handleIndex);
				me._handleIndex = null;
				me._clickOffset = null;
				me._animateOff = false;
				postMouseEvent(me, xye, id);
			};

			var fOutTrack = function(event, id) {
				clearEnds(me);
			};

			var fMoveTrack = function(event, id) {
				doMouseCapture(me, event, OBJ_TRACK, true);
			};

			handleCount = (o.values && o.values.length) || 1;

			for (var i = 0; i < handleCount; i++) {
				handles.push(handle);
			}

			this.handles = existingHandles.add($(
					handles.join(""))
					.appendTo(this.element));

			for (var i = 0; i < handleCount; i++) {
				handle = this.handles[i];
				handle.index = i;
				J2S.setDraggable(handle, [ fDown, fDrag, fUp ]);
			}
			
			if (handleCount == 1) {
				$(this.element).mousedown(fDownTrack);
				if (this.isScrollBar) {
					$(this.element).mousemove(fMoveTrack);
					$(this.element).mouseup(fUpTrack);
					$(this.element).mouseout(fOutTrack);
				} else {
					$(this.element).closest(".ui-j2sslider-wrap").mousedown(fDownWrap);
				}
			}
			
			this.handle = this.handles.eq(0);
			this.handles.add(this.range).filter("a").click(function(event) {event.preventDefault();})
			this.handles.each(function(i) {$(this).data("ui-j2sslider-handle-index", i);});
			this._refreshValue();
			this._animateOff = false;
		},
		
		_destroy : function() {
			
			for (var i = 0; i < this.handles.length; i++) {
				J2S.setDraggable(this.handles[i], false);
			}
			

			this.handles.remove();
			this.range.remove();

			this.element.removeClass(
					"ui-j2sslider"
					+ " ui-j2sslider-horizontal"
					+ " ui-j2sslider-vertical"
					+ " ui-j2sscrollbar-horizontal"
					+ " ui-j2sscrollbar-vertical"
					+ " ui-j2sslider-disabled"
					+ " ui-widget" 
					+ " ui-widget-content"
					+ " ui-corner-all");

			this._mouseDestroy();
		},

		_detectOrientation : function() {
			this.orientation = (this.options.orientation === "vertical") ? "vertical"
					: "horizontal";
		},
		_resetClass : function() {
			var type = (this.isScrollBar ? "scrollbar" : "slider");
			this.element
					.removeClass(
							"ui-j2sscrollbar-horizontal ui-j2sscrollbar-vertical ui-j2sslider-horizontal ui-j2sslider-vertical")
					.addClass(
							"ui-j2s" + type + "-"
									+ this.orientation);
			if (this.isScrollBar)
				this.element.removeClass("ui-widget-content");
		},
		_start : function(event, index) {
			var uiHash = {
				handle : this.handles[index],
				value : this.value()
			};
			if (this.options.values
					&& this.options.values.length) {
				uiHash.value = this.values(index);
				uiHash.values = this.values();
			}
			return this._trigger("start", event, uiHash);
		},

		_slide : function(event, index, newVal) {
			var otherVal, newValues, allowed;

			if (this.options.values
					&& this.options.values.length) {
				otherVal = this.values(index ? 0 : 1);

				if ((this.options.values.length === 2 && this.options.range === true)
						&& ((index === 0 && newVal > otherVal) || (index === 1 && newVal < otherVal))) {
					newVal = otherVal;
				}

				if (newVal !== this.values(index)) {
					newValues = this.values();
					newValues[index] = newVal;
					// A slide can be canceled by returning
					// false from the slide callback
					allowed = this
							._trigger(
									"slide",
									event,
									{
										handle : this.handles[index],
										value : newVal,
										values : newValues
									});
					otherVal = this.values(index ? 0 : 1);
					if (allowed !== false) {
						this.values(index, newVal, true);
					}
				}
			} else {
				if (newVal !== this.value()) {
					// A slide can be canceled by returning
					// false from the slide callback
					allowed = this
							._trigger(
									"slide",
									event,
									{
										handle : this.handles[index],
										value : newVal
									});
					if (allowed !== false) {
						this.value(newVal);
					}
				}
			}
		},

		_stop : function(event, index) {
			var uiHash = {
				handle : this.handles[index],
				value : this.value()
			};
			if (this.options.values
					&& this.options.values.length) {
				uiHash.value = this.values(index);
				uiHash.values = this.values();
			}

			this._trigger("stop", event, uiHash);
		},

		_change : function(event, index) {
			if (!this._keySliding && !this._mouseSliding) {
				var uiHash = {
					handle : this.handles[index],
					value : this.value()
				};
				if (this.options.values
						&& this.options.values.length) {
					uiHash.value = this.values(index);
					uiHash.values = this.values();
				}

				this._trigger("change", event, uiHash);
			}
		},

		getState : function() {
			return this.options
		},

		value : function(newValue) {
			if (arguments.length) {

				this.options.value = this
						._trimAlignValue(newValue);
				this._refreshValue();
				this._change(null, 0);
				return;
			}

			return this._value();
		},

		values : function(index, newValue) {
			var vals, newValues, i;

			if (arguments.length > 1) {
				this.options.values[index] = this
						._trimAlignValue(newValue);
				this._refreshValue();
				this._change(null, index);
				return;
			}

			if (arguments.length) {
				if ($.isArray(arguments[0])) {
					vals = this.options.values;
					newValues = arguments[0];
					for (var i = 0; i < vals.length; i += 1) {
						vals[i] = this
								._trimAlignValue(newValues[i]);
						this._change(null, i);
					}
					this._refreshValue();
				} else {
					if (this.options.values
							&& this.options.values.length) {
						return this._values(index);
					} else {
						return this.value();
					}
				}
			} else {
				return this._values();
			}
		},

		_setOption : function(key, value) {
			var i, valsLength = 0;

			if ($.isArray(this.options.values)) {
				valsLength = this.options.values.length;
			}

			$.Widget.prototype._setOption.apply(this,
					arguments);

			switch (key) {
			case "disabled":
				if (value) {
					this.handles.filter(".ui-state-focus")
							.blur();
					this.handles
							.removeClass("ui-state-hover");
					this.handles.prop("disabled", true);
					this.element.addClass("ui-disabled");
				} else {
					this.handles.prop("disabled", false);
					this.element.removeClass("ui-disabled");
				}
				break;
			case "orientation":
				this._detectOrientation();
				this._resetClass();
				this._refreshValue();
				break;
			case "value":
				this._animateOff = true;
				this._refreshValue();
				this._change(null, 0);
				this._animateOff = false;
				break;
			case "values":
				this._animateOff = true;
				this._refreshValue();
				for (var i = 0; i < valsLength; i += 1) {
					this._change(null, i);
				}
				this._animateOff = false;
				break;
			case "min":
			case "max":
				this._animateOff = true;
				this._refreshValue();
				this._animateOff = false;
			break;
			case "visibleAmount":
				this.isScrollBar = true;
				this.visibleAmount = value;
				var min = this._valueMin();
				var max = this._valueMax();
				var f = (value >= 0 && min + value <= max ? 
					 value * 1 / (max - min) : 0.1);
				this.visibleFraction = f;
				if (f < 0.1)
					f = 0.1;
				this.handleFraction = f;
				var hw = length(this);
				if (this.orientation === "horizontal")
					$(this.handles[0]).width(this.handleSize = f * hw);
				else
					$(this.handles[0]).height(this.handleSize = f * hw);
				this.visibleAdjust = (f - this.visibleFraction) * hw;
				this._animateOff = true;
				this._resetClass();
				this._refreshValue();
				this._animateOff = false;
			break;
			}
		},

		// internal value getter
		// _value() returns value trimmed by min and max,
		// aligned by step
		_value : function() {
			return this._trimAlignValue(this.options.value);
		},

		// internal values getter
		// _values() returns array of values trimmed by min
		// and max, aligned by step
		// _values( index ) returns single value trimmed by
		// min and max, aligned by step
		_values : function(index) {
			if (arguments.length) {
				return this._trimAlignValue(this.options.values[index]);
			} 
			// .slice() creates a copy of the array
			// this copy gets trimmed by min and max and
			// then returned
			var vals = this.options.values.slice();
			for (var i = 0; i < vals.length; i += 1) {
				vals[i] = this._trimAlignValue(vals[i]);
			}
			return vals;
		},

		_getPosition : function(event) {
			var position = (event.pageX || event.pageX === 0 ?
				{
					x : event.pageX,
					y : event.pageY
				} : {
					// touch event? get position from touch
					x : event.originalEvent.touches[0].pageX,
					y : event.originalEvent.touches[0].pageY
				});
			return position;
		},
		
		// returns the step-aligned value that val is
		// closest to, between (inclusive) min and max
		_trimAlignValue : function(val) {
			if (val <= this._valueMin()) {
				return this._valueMin();
			}
			var max = Math.round(this._valueMax() - this.visibleAmount); //* (1-this.handleFraction)
			if (val >= max) {
				return max;
			}
			var step = (this.options.step > 0) ? this.options.step
					: 1, valModStep = (val - this
					._valueMin())
					% step, alignValue = val - valModStep;

			if (Math.abs(valModStep) * 2 >= step) {
				alignValue += (valModStep > 0) ? step
						: (-step);
			}

			// Since JavaScript has problems with large
			// floats, round
			// the final value to 5 digits after the decimal
			// point (see #4124)

			return Math.round(alignValue);// parseFloat(alignValue.toFixed(5));
		},

		_valueMin : function() {
			return this.options.min;
		},

		_valueMax : function() {
			return this.options.max;
		},

		_getValPercent : function(i) {
			var dif = this._valueMax() - this._valueMin();
			var valPercent = (dif == 0 ? 0
					: ((i >= 0 ? this.values(i) : this
							.value()) - this._valueMin())
							/ dif * 100);
			return (this.options.inverted && !this.isScrollBar ? 100 - valPercent
					: valPercent);
		},

		_refreshValue : function() {
			var lastValPercent, valPercent, value, valueMin, valueMax;
			var o = this.options;
			var oRange = o.range;
			var that = this;
			var animate = (!this._animateOff) ? o.animate : false;
			var _set = {};
			if (this.options.values
					&& this.options.values.length) {
				this.handles
						.each(function(i) {
							valPercent = that
									._getValPercent(i);
							_set[that.orientation === "horizontal" ? "left"
									: "bottom"] = valPercent
									+ "%";
							$(this).stop(1, 1)[animate ? "animate"
									: "css"](_set,
									o.animate);
							if (that.options.range === true) {
								if (that.orientation === "horizontal") {
									if (i === 0) {
										that.range.stop(1,
												1)[animate ? "animate"
												: "css"]
												(
														{
															left : valPercent
																	+ "%"
														},
														o.animate);
									}
									if (i === 1) {
										that.range[animate ? "animate"
												: "css"]
												(
														{
															width : (valPercent - lastValPercent)
																	+ "%"
														},
														{
															queue : false,
															duration : o.animate
														});
									}
								} else {
									if (i === 0) {
										that.range.stop(1,
												1)[animate ? "animate"
												: "css"]
												(
														{
															bottom : (valPercent)
																	+ "%"
														},
														o.animate);
									}
									if (i === 1) {
										that.range[animate ? "animate"
												: "css"]
												(
														{
															height : (valPercent - lastValPercent)
																	+ "%"
														},
														{
															queue : false,
															duration : o.animate
														});
									}
								}
							}
							lastValPercent = valPercent;
						});
			} else {
				// just one handle
				valPercent = this._getValPercent(-1);
				var isHorizontal = (this.orientation === "horizontal");
				var val = (valPercent * getPixelTotal(this)/100) + "px";									
				_set[isHorizontal ? "left"
						: this.isScrollBar ? "top" : "bottom"] = val;
				this.handle.stop(1, 1)[animate ? "animate"
						: "css"](_set, o.animate);

				if (oRange === "min" && isHorizontal) {
					this.range.stop(1, 1)[animate ? "animate"
							: "css"]({
						width : valPercent + "%"
					}, o.animate);
				}
				if (oRange === "max" && isHorizontal) {
					this.range[animate ? "animate" : "css"]
							({
								width : (100 - valPercent)
										+ "%"
							}, {
								queue : false,
								duration : o.animate
							});
				}
				if (oRange === "min"
						&& this.orientation === "vertical") {
					this.range.stop(1, 1)[animate ? "animate"
							: "css"]({
						height : valPercent + "%"
					}, o.animate);
				}
				if (oRange === "max"
						&& this.orientation === "vertical") {
					this.range[animate ? "animate" : "css"]
							({
								height : (100 - valPercent)
										+ "%"
							}, {
								queue : false,
								duration : o.animate
							});
				}
			}
		}

	});
};

})(J2S.__$);

// BH 12/17/2018; 2020.01.25
