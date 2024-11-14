/**
 *  NOTE: If you change this file, copy its contents into 
 *  its spot at the end of JQueryUI.java. Or, if you are testing
 *  work here and set JQueryUI.loadResourceInternal = false only while testing,
 *  then do that paste.
 * 

 */

// BH 2023.06.06 no-move menu actuation; adds cursor:pointer to ui-j2s-menu to enable click; adds pointerdown/up; 
//   see https://stackoverflow.com/questions/3025348/how-do-i-use-jquery-for-click-event-in-iphone-web-application/4910962#4910962
// based on jQuery UI - v1.9.2 - 2012-12-17

;(function(Swing, $) {

J2S.__makeMenu = function() {
// run once; set to NOP
J2S.__makeMenu = function(){};	


 var MODE_UNKNOWN = 0;
 var MODE_TOUCH = 1;
 var MODE_MOUSE = 2;

 var outActive;
 var vart;

 var n=!1, e = $;

 // local methods here for help with debugging
	
 
 // BH note that swingjs.plaf.JSButton will set and clear ui-state-disabled on its own
 

 var delayMe = function(element, f, ms) {
	 var id = element._delay(f, ms);
	 return id;
 }	
 
 var clearMe = function(id, why) {
	 return clearTimeout(id);
 }

 var someMenuOpen = function() {
	 return $(".swingjsPopupMenu:visible").length > 0;
 }
 
 var closeOnLeave = function(me, t) {
	 	$.contains(me.element[0],me.document[0].activeElement)
	 	  ||me.collapseAll(t)
 }
 
 var cleanText = function(n) {
	 return n && n.innerText.replace(/\n/g,"|");
 }
 
 var CLICK_OUT_DELAY = 200;// ms; 100 was too fast
 var CLOSE_DELAY = 700;
 
 var setCloseTimer = function(me) {
	 if (vart)
		 clearTimeout(vart);
     vart = me.t = setTimeout(function() {  	 
    	 me._getT();
    	 me._stopT("closeTimer collapsing");
    	 me.collapseAll();
 		  vart = me.t = 0;
 	  },CLOSE_DELAY);
 }
 var setCollapseTimer = function(me, t) {
	 //System.err.println("collapseAll " + me.uuid);
	 me.timer = delayMe(me,
       function(){
		 doCmd("clearClickOut", me);
		 if (!someMenuOpen())
			 return;
		 var r=n?me.element:e(t&&t.target).closest(me.element.find(".ui-j2smenu"));
		 r.length||(r=me.element);
		 me._closeSubmenus(r);
		 me.unsetFocus(t);
		 me.activeMenu=r;
		 me.closed = true;
	   }, me.delay); 

 }
 
 var myMenuItem = function(t) { return $(t).closest(".ui-j2smenu-item") }
 var myMenuBar = function(t) { return $(t).closest(".j2s-menuBar-menu") }
 var myMenu = function(t) { return $(t).closest(".ui-j2smenu") }
 var isPopupMenu = function(t) { return t && t.is(".ui-j2s-popup-menu") }
 var isDisabled = function(t) { return t && t.is(".ui-state-disabled") }
 var isMenu = function(t) { return t && (t.has(".ui-j2smenu").length > 0) }
 
 var doCmd = function(trigger,me,t,n,why) {
	 
	 var debug = function(){}
	 
	 debug("j2sMenu trigger " + trigger + " " + (me.active && me.active[0].innerText.split("\n").join("-")))
	 
	 why || (why = "");
	 var event = t;
	 var target = (!t || !t.target ? null : myMenuItem(t.target)[0]);

	 switch(trigger) {
	 case "onoutn":
		 me._closeMe();
		 break;
	 case "onmoven":
		 me.clickoutDisabled = false;
		 if ($(target).hasClass("ui-j2smenu")) {
			 // this is the most likely way we will leave, via a mousemove on the border
			 me._closeMe();  
			 break;
		 }
	 case "onmovep":
	 case "onovern":
		 me.clickoutDisabled = false;
		 me._stopT(trigger);
		 if (!t)return;
		 // BH 2018
		 // -- added stopPropagation
		 // -- changed to mouseover from mouseenter, since we have children
		 var a = myMenuItem(target)
		 if (a.hasClass(".ui-state-focus"))
			 return;		 
		 if (!a.hasClass("j2s-popup-menu") && !a.hasClass("ui-j2smenu-node")) {
			 me._closeSubmenus(a.parent());			 
		 }
		 var m = a;
		 //testing a = a.find(".a");
		 a[0] && a[0].focus();
		 var n=myMenuItem(t.currentTarget);
		 n.siblings().children(".ui-state-active").removeClass("ui-state-active");
		 t.stopPropagation();
		 me.setFocus(t,n);
		 t = m;
		 break;
	 case "onpress":
		var n=myMenuItem(target);
		me.setFocus(t,n);
		me.elementPressed = n[0];
	 case "onrelease":
	 case "onclick":
		var n=myMenuItem(target);
		if (isDisabled(n) || n[0] != me.elementPressed)
			return;
		if (isDisabled(n.first()))
			break;
		me.select(t);
		var doOpen = isPopupMenu(n.first());
		if (doOpen) {
			// must disable clickout in progress, or a click here will close all menus just after expanding
			me.clickoutDisabled = true;
			me.expand(t);
		} else {
			if (!me.element.is(":focus")) {
				me.element.trigger("setFocus",[!0]);
				me.active&&me.active.parents(".ui-j2smenu").length===1&&clearMe(me.timer, trigger);
			} 
			if (me.mouseState != MODE_TOUCH)
				doCmd("collapseAll", me, 0, 1);			 
		}
		break;
	 case "clearClickOut":
		 me._off(me.document, "click");
		 outActive = null;
		 me._stopT("clearClickOut");
		 return;
	 case "setClickOut":
		 if (me.clickoutDisabled)
			 return;
		 if (outActive)
			 doCmd("clearClickOut", outActive);
		 setTimeout(function(){	
			 if (me.clickoutDisabled)
				 return;
			 outActive = me;
			 me._on(me.document,{ "click":function(t){doCmd("onclick_out", me, $, t),n=!1}});			 			 
		 },CLICK_OUT_DELAY);
		 return;
	 case "onclick_out":
		 if (me.clickoutDisabled || outActive != me || !someMenuOpen()) {
			 me.clickoutDisabled = false;
			 doCmd("clearClickOut", me);
			 return;
		 }		 
		 myMenuBar(target).length == 0 && (myMenu(target).length||me.collapseAll(t));
	 	return;
	 case "onleave":
		 if (me.mouseState != MODE_TOUCH)
		   me._closeMe("onleave");
		 return;
	 case "onfocus":
		 n||me.setFocus(t,me.active||me.element.children(".ui-j2smenu-item").eq(0));
		 return;
	 case "onblur":
		 me.timer = delayMe(me, function(){closeOnLeave(me, t)});
		 break;
	 case "_activate":
		 isDisabled(me.active)||(me.active.children(".a[aria-haspopup='true']").length?me.expand(t):me.select(t));
		 break;
	 case "_startOpening":
		 if(t.attr("aria-hidden")!=="true" && t.css('display') !== 'none') {
			 return;
		 }
		 me.closed = false;
		 //me.timer=delayMe(me, function(){
			 me._closeSubmenus(),me._openSubmenu(t);
		//	 },me.delay);
		 return;
	 case "_hidePopupMenu":
		 // trigger Java to deselect these - the JMenu class
		 me.mouseState = MODE_UNKNOWN; // unknown
		 t = me.element.find(".ui-j2smenu[aria-hidden!=true]").attr("aria-hidden","true").parent();
		 var a = me;
		 doCmd("_hide", a, a.element);
		 if (!t[0])
			 return;
		 break;
	 case "_openSubmenu":
		 n||(n = me.active || me.activeMenu);
		 if (isDisabled(n))
			 return;
		 var item = n[0].firstElementChild;
		 var li = n;
		 n = e.extend({of:n},me.options.position);
		 me._stopT("opening");
		 clearMe(me.timer, trigger);
		 var ui = me.activeMenu && me.activeMenu[0] && me.activeMenu[0]["data-ui"];
	 	 ui && ui.processJ2SMenuCmd$OA([trigger,me,null,t.parent(),n,why]);
		 // adds role=xxxx
	 	 me.refresh("_openSubmenu",n);
		 // adds mouse binding to role=menuitem
	 	 ensureMouseSet(ui.popupMenu, li);
		 var v = me.element.find(".ui-j2smenu").not(t.parents(".ui-j2smenu"));
		 doCmd("_hide", me, v);
		 try {
			 // required if menu has been modified
			 doCmd("_show", me, me.activeMenu);
			 doCmd("_show", me, t);
			 t.removeAttr("aria-hidden").attr("aria-expanded","true").position(n);
			 me.closed = false;
		 } catch(err){
			 System.err.println("j2sMenu error: " + err);
		 }
		 return;
	 case "closeSiblingMenus":
		 var m = t.closest("ul").find(".ui-state-active")
		 m.removeClass("ui-state-active");
		 var v = t.find(".ui-j2smenu");
		 if (!v.length)
			 return;
		 doCmd("_hide", me, v);
		 v.attr("aria-hidden","true").attr("aria-expanded","false");
		 t = v.parent();
		 return;
	 case "_closeSubmenus":
		 var a = me.active;
		 if (a && a[0] && a[0]["data-component"].getUIClassID$() != "MenuUI")
			return;
		 t||(t=me.active?me.active.parent():me.element);
		 var m = t.closest("ul").find(".ui-state-active")
		 m.removeClass("ui-state-active");
		 var v = t.find(".ui-j2smenu");
		 if (!v.length)
			 return;
		 doCmd("_hide", me, v);
		 v.attr("aria-hidden","true").attr("aria-expanded","false");
		 t = v.parent();
		 break;
	 case "_move":
		 var a = n[0];
		 var b = n[1];
		 var r = me.active&&
				 (a==="first"||a==="last"? me.active[a==="first"?"prevAll":"nextAll"](".ui-j2smenu-item").eq(-1)
				   : me.active[a+"All"](".ui-j2smenu-item").eq(0));
		 if(!r||!r.length||!me.active)
			 r=me.activeMenu.children(".ui-j2smenu-item")[b]();
		 me._closeSubmenus(r);
		 me.setFocus(t,r);
		 return;
	 case "_show":
		 t.show();
		 break;
	 case "_hide":
		 if (!t[0])
			 return;
		 t.hide();
		 break;
	 case "expand":
    	 if (!someMenuOpen() || isDisabled(me.active))
			 return;
		 n = me.active&&me.active.children(".ui-j2smenu").children(".ui-j2smenu-item").first();
		 if (n&&n.length) {
			 me._openSubmenu(n.parent());
			 //me.timer = delayMe(me,function(){
				 me.setFocus(t,n)
				// });
		 }
		 break;
	 case "collapse":
		 if (!someMenuOpen())
			 return;
		 me._closeSubmenus();
		 var v=me.active&&me.active.parent().closest(".ui-j2smenu-item",me.element);
		 if (v && v.length) {
			 me.setFocus(t,v);
			 me._closeSubmenus(v);
		 } else {
			 doCmd("collapseAll", me, 0, 1);			 
		 }
		 break;
	 case "collapseAll":
		 // touch needs this setTimeout to delay close action until touch is processed
		 setTimeout(function() {
			 if (me.closed || me.clickoutDisabled) {
				 return;
			 }
			 doCmd("_hidePopupMenu", me);
			 clearMe(me.timer, trigger),
			 setCollapseTimer(me, t)
		 }, 100);
		 break;
	 case "setFocus":
		 me.clickoutDisabled = true;
		 // we determine this to be a touch because a 
		 // focus from DOWN is made before a focus from OVER or MOVE
		 if (event.type == "pointermove" || event.type == "pointerover")
			 me.mouseState = MODE_MOUSE;// TODO menu only
		 else if (event.type == "pointerdown" && me.mouseState == MODE_UNKNOWN)
			 me.mouseState = MODE_TOUCH;
		 var a = n.first();
		 var u=n.children(".ui-j2smenu");
		 var subIsActive = (a[0] == (me.active && me.active[0]));
		 if (u.length == 0 && subIsActive)
			 return;
		 me.unsetFocus(t,t&&t.type==="focus", "fromSetFocus");
		 me._scrollIntoView(n);
		 // BH added 2024.01.16
		 n.siblings().each(function(a,b){
				 doCmd("closeSiblingMenus", me, $(b));
		 })
		 me.active=a;
		 var r=me.active.addClass("ui-state-focus");
		 //testing var r=me.active.children(".a").addClass("ui-state-focus");
		 me.options.role&&me.element.attr("aria-activedescendant", r.attr("id"));
		 myMenuItem(me.active.parent()).children(".a:first").addClass("ui-state-active");
		 u.length&&(/^pointer/.test(t.type) || /^mouse/.test(t.type))&&me._startOpening(u);
		 me.activeMenu=n.parent();
		 me._trigger("focus",t,{item:n});
		 t = n;
		 break;
	 case "unsetFocus":
		 if (me.active && t && typeof n == "undefined" && t.relatedTarget && t.relatedTarget.getAttribute("role") != "presentation") {
			 doCmd("_hide", me, t, me.element);
		 }
		 n||clearMe(me.timer);
		 if(!me.active)return;
		 me.active.removeClass("ui-state-focus");
		 // testing me.active.children(".a").removeClass("ui-state-focus");
		 var a = me.active;
		 me.active=null;
		 me._trigger("blur",t,{item:a});
		 t = a;
		 break;
	 case "select":
		 if (me.mouseMode == MODE_MOUSE) {
			 return;
		 } 
		 me.active=me.active||myMenuItem(target);
		 if (isMenu(me.active)) {
			 // the anchor element is the first child.
			me.clickoutDisabled = !me.active.children().first().is(".ui-state-active");
		 } else {
			me.collapseAll(t,!0);
		 }
		 me._trigger("select",t,{item:me.active});
		 if (!t[0]) {
			 return;
		 }
		 break;
	 case "refresh":
		 n=me.options.icons.submenu;
		 var role=me.options.role;
		 var r=me.element.find(me.options.menus);
		 t = r.filter(":not(.ui-j2smenu)")
		   .addClass("ui-j2smenu ui-widget ui-widget-content ui-corner-all");
		 doCmd("_hide", me, t);
		 t.attr({role:me.options.role,"aria-hidden":"true","aria-expanded":"false"})
		   .each(function(){
			   var t=e(this),r=t.prev(".a"),
			   i=e("<span>").addClass("ui-j2smenu-icon ui-icon "+n)
			   .attr({role:role})
			   .data("ui-j2smenu-submenu-carat",!0);
			   r.attr("aria-haspopup","true").prepend(i);
			   t.attr("aria-labelledby",r.attr("id"));
		   });
		 t=r.add(me.element);
		 t.children(":not(.ui-j2smenu-item):has(.a)").addClass("ui-j2smenu-item")
		   		.attr("role","presentation").children(".a").uniqueId()
		   		.addClass("ui-corner-all").attr({tabIndex:-1,role:"menuitem"});
		 t.children(":not(.ui-j2smenu-item)").addClass("ui-widget-content ui-j2smenu-divider");
		 t.children(".ui-state-disabled").attr("aria-disabled","true");
		 me.active&&!e.contains(me.element[0],me.active[0])&&me.unsetFocus();
		 return;
	 case "keyActivate":
		 
		 // BH 1/15/2019 key mnemonics
		 
		 var node = e(".j2s-popup-menu  > :visible.ui-mnem-" + Character.toLowerCase$I(t.keyCode));
		 switch (node.length) {
		 case 0:
			 doCmd("onclick", me, t);
			 break;
		 case 1:
			 doCmd("_openSubmenu", me, node.next("ul"), node);
			 break;
		 default:
			 // ignore multiple hits
			 break;
		 }
		 break;
	 }
	 
	 var ui = me.activeMenu && me.activeMenu[0] && me.activeMenu[0]["data-ui"];
 	 ui && ui.processJ2SMenuCmd$OA([trigger,me,event,t,n,target,why]);
 }
 
$.widget("ui.j2smenu",{
 version:"1.9.2",
 defaultElement:"<ul>",
 delay:300,
 options:{icons:{submenu:"ui-icon-carat-1-e"},
 menus:"ul",
 position:{my:"left top",at:"right top"},
 role:"j2smenu",
 blur:null,
 focus:null,
 select:null,
 jPopupMenu:null
 },
 
 
 _create:function(){

	 this.t = this.timer = 0;
	 
	 this.closed = false;
	 
//	 if (typeof this.options.delay == "number")
//		 this.delay = this.options.delay;
	 
	 this.activeMenu=this.element,
	 this.element.uniqueId().addClass("ui-j2smenu ui-widget ui-widget-content ui-corner-all")
	   .toggleClass("ui-j2smenu-icons",!!this.element.find(".ui-icon").length)
	   .attr({role:this.options.role,tabIndex:0})
	   .bind("click"+this.eventNamespace,e.proxy(function(e){ this.options.disabled&&e.preventDefault() },this)),
	 this.options.disabled&&this.element.addClass("ui-state-disabled").attr("aria-disabled","true"),
	 this._on({
		 "click .ui-state-disabled > .a":	function(t){ t.preventDefault() },
		 "click .ui-j2smenu-item:has(.a)":	function(t){ doCmd("onclick",this,t);},
                 "pointerdown .ui-j2smenu-item > .a":   function(t){ doCmd("onpress",this,t) },
                 "pointerup .ui-j2smenu-item > .a":     function(t){ doCmd("onrelease",this,t) },
		 "mousedown .ui-j2smenu-item > .a":	function(t){ doCmd("onpress",this,t) },
		 "mouseup .ui-j2smenu-item > .a":	function(t){ doCmd("onrelease",this,t) },
		 "mousemove .swingjsPopupMenu ":	function(t){ doCmd("onmovep",this,t,0); },
		 "mouseleave .ui-j2smenu":			function(t){ doCmd("onleave",this,t); },
		 "mousemove .ui-j2smenu-node":		function(t){ doCmd("onmoven",this,t,0); },
		 "mouseout  .ui-j2smenu-node":		function(t){ doCmd("onoutn",this,t,0); },
		 "mouseover .ui-j2smenu-node":		function(t){ doCmd("onovern",this,t,0); },		 
		 mouseleave:						function(t){ doCmd("onleave",this,t); },
		 blur:								function(t){ doCmd("onblur",this,t)},
		 focus:								function(t,n){ doCmd("onfocus",this,t,n); },
		 keydown:		"_keydown"
	 }), 
	 this.refresh("create");
 	},
 _destroy:function(){this.element.removeAttr("aria-activedescendant").find(".ui-j2smenu").andSelf()
	 .removeClass("ui-j2smenu ui-widget ui-widget-content ui-corner-all ui-j2smenu-icons")
	 .removeAttr("role").removeAttr("tabIndex").removeAttr("aria-labelledby").removeAttr("aria-expanded")
	 .removeAttr("aria-hidden").removeAttr("aria-disabled").removeUniqueId().show(),
	 this.element.find(".ui-j2smenu-item").removeClass("ui-j2smenu-item")
	 .removeAttr("role").removeAttr("aria-disabled")
	 .children(".a").removeUniqueId().removeClass("ui-corner-all ui-state-hover")
	 .removeAttr("tabIndex").removeAttr("role").removeAttr("aria-haspopup").children().each(function(){var t=e(this);t.data("ui-j2smenu-submenu-carat")&&t.remove()}),this.element.find(".ui-j2smenu-divider").removeClass("ui-j2smenu-divider ui-widget-content")
	 },
 _keydown:function(t){
	t.preventDefault();	 	
	 var n,r,i,s,o,u=!0;
	 switch(t.keyCode){
	 case 16:
	 case 17:
	 case 18: // CTRL ALT SHIFT alone
		 break;
	 case e.ui.keyCode.PAGE_UP:
		 this.previousPage(t);
		 break;
	 case e.ui.keyCode.PAGE_DOWN:
		 this.nextPage(t);
		 break;
	 case e.ui.keyCode.HOME:
		 this._move("first","first",t);
		 break;
	 case e.ui.keyCode.END:
		 this._move("last","last",t);
		 break;
	 case e.ui.keyCode.UP:
		 this.previous(t);
		 break;
	 case e.ui.keyCode.DOWN:
		 this.next(t);
		 break;
	 case e.ui.keyCode.LEFT:
		 this.collapse(t);
		 break;
	 case e.ui.keyCode.RIGHT:
		 this.active && !isDisabled(this.active) && this.expand(t);
		 break;
	 case e.ui.keyCode.ENTER:
	 case e.ui.keyCode.SPACE:
		 this._activate(t);
		 break;
	 case e.ui.keyCode.ESCAPE:
		 
		 this.collapse(t);
		 break;
	 default:
		doCmd("keyActivate",this, t, true);
		break;
	}
 },

 on: function(a, x) {for(var i = a.length; --i >= 0;)this._on(a[i],x)},
 on2: function(obj, evts, handle) {var a = {};for(var i = evts.length; --i >= 0;)a[evts[i]]=handle;this._on(obj, a)},
 _stopT: function(why) {
	  clearTimeout(this.t);
	  this.t = 0;
 },
 _getT: function() {vart = this.t;},
 _closeMe: function() {
     if (this.t){
    	 this._stopT("closeMe");
     }
     if (vart){
    	 this.t = vart;
    	 this._stopT("closeMe");
    	 vart = 0;
     }
     setCloseTimer(this);
 },

 mouseState:MODE_UNKNOWN,
 
 _activate:function(t){     doCmd("_activate", this, t); },
 _startOpening: function(t){ doCmd("_startOpening", this, t); },
 setFocus:function(t,n){       doCmd("setFocus", this, t, n) },
 unsetFocus:function(t,n){        doCmd("unsetFocus", this, t, n);},
 _openSubmenu:function(t){  doCmd("_openSubmenu", this, t);},
 _closeSubmenus:function(t){doCmd("_closeSubmenus", this, t, n);},
 collapseAll:function(t,n,why){ doCmd("collapseAll",this, t, n, why);},
 collapse:function(t){      doCmd("collapse", this, t);},
 refresh:function(t,n){     doCmd("refresh", this, t, n); },
 expand:function(t){        doCmd("expand", this, t);},
 select:function(t){        doCmd("select", this, t); },
 setClickOut:function() {    doCmd("setClickOut", this); }, 
 next:function(t){this._move("next","first",t)},
 previous:function(t){this._move("prev","last",t)},
 _scrollIntoView:function(t){var n,r,i,s,o,u;this._hasScroll()&&(n=parseFloat(e.css(this.activeMenu[0],"borderTopWidth"))||0,r=parseFloat(e.css(this.activeMenu[0],"paddingTop"))||0,i=t.offset().top-this.activeMenu.offset().top-n-r,s=this.activeMenu.scrollTop(),o=this.activeMenu.height(),u=t.height(),i<0?this.activeMenu.scrollTop(s+i):i+u>o&&this.activeMenu.scrollTop(s+i-o+u))},
 isFirstItem:function(){return this.active&&!this.active.prevAll(".ui-j2smenu-item").length},
 isLastItem:function(){return this.active&&!this.active.nextAll(".ui-j2smenu-item").length},
 nextPage:function(t){var n,r,i;if(!this.active){this.next(t);return}if(this.isLastItem())return;this._hasScroll()?(r=this.active.offset().top,i=this.element.height(),this.active.nextAll(".ui-j2smenu-item").each(function(){return n=e(this),n.offset().top-r-i<0}),this.setFocus(t,n)):this.setFocus(t,this.activeMenu.children(".ui-j2smenu-item")[this.active?"last":"first"]())},
 previousPage:function(t){var n,r,i;if(!this.active){this.next(t);return}if(this.isFirstItem())return;this._hasScroll()?(r=this.active.offset().top,i=this.element.height(),this.active.prevAll(".ui-j2smenu-item").each(function(){return n=e(this),n.offset().top-r+i>0}),this.setFocus(t,n)):this.setFocus(t,this.activeMenu.children(".ui-j2smenu-item").first())},

 _move:function(a,b,t){ doCmd("_move", this, t, [a,b]);},
 _hasScroll:function(){return this.element.outerHeight()<this.element.prop("scrollHeight")}
 })

Swing.menuCounter = 0;
Swing.menuInitialized = 0;

Swing.__getMenuStyle = function(applet) { return '\
	.swingjsPopupMenu{font-family:Arial,sans-serif;font-size:11px;position:absolute;z-index:'+J2S.getZ(applet, "menu")+'}\
	.swingjsPopupMenu,.swingjsPopupMenu .ui-corner-all{border-radius:5px}\
	.swingjsPopupMenu,.swingjsPopupMenu .ui-widget-content{border:1px solid #a6c9e2;background-color:#fcfdfd;color:#222}\
	.swingjsPopupMenu .a{color:#222;font-size:10px;}\
	.swingjsPopupMenu input[type="checkbox"]{vertical-align:middle;}\
	.swingjsPopupMenu,.swingjsPopupMenu .ui-j2smenu{list-style:none;padding:2px;margin:0;display:block;outline:none;box-shadow:1px 1px 5px rgba(50,50,50,0.75)}\
	.swingjsPopupMenu .ui-j2s-menuBar-menu:focus{outline:none;background:#d0e5f5}\
	.swingjsPopupMenu .ui-j2smenu{cursor:pointer;outline:none;margin-top:-3px;position:absolute}\
	.swingjsPopupMenu .ui-j2smenu-item{outline:none;cursor:pointer;margin:0 0 0 0;padding:0.1em;width:100%}\
	.swingjsPopupMenu .a:focus{outline:none;cursor:pointer;margin:0 0 0 0;padding:0.1em}\
	.swingjsPopupMenu .ui-j2smenu-divider{position:absolute;margin:3px 1px;height:0;transform:translateY(-0.2em);font-size:1;line-height:1px;border-width:1px 0 0 0;width:93%;}\
	.swingjsPopupMenu .ui-j2smenu-item .a{display:block;padding:0.05em 0.4em;white-space:nowrap;border:1px solid transparent}\
	.swingjsPopupMenu .ui-j2smenu-icons{position:relative}\
	.swingjsPopupMenu .ui-j2smenu-icons .ui-j2smenu-item .a{position:relative;padding-left:2em}\
	.swingjsPopupMenu .ui-icon{display:block;text-indent:-99999px;overflow:hidden;background-repeat:no-repeat;position:absolute;top:.2em;left:.2em}\
	.swingjsPopupMenu .ui-j2smenu-icon{position:static;float:right}\
	.swingjsPopupMenu .ui-icon-alt-y{min-width:30ex;text-align:right;background-image:none;background-position:0 0}\
	.swingjsPopupMenu .ui-icon-alt-y:after{content:"alt-Y"}\
	.swingjsPopupMenu .ui-icon-alt-shift-x:{min-width:130ex;text-align:right;background-image:none;background-position:0 0}\
	.swingjsPopupMenu .ui-icon-alt-shift-x:after{content:"alt-shift-X"}\
	.swingjsPopupMenu .ui-icon-carat-1-e{min-width:1ex;text-align:right;background-image:none;background-position:0 0}\
	.swingjsPopupMenu .ui-icon-carat-1-e:after{content:"\\0025B6"}\
	.swingjsPopupMenu .ui-state-default{border:1px solid #c5dbec;background:#dfeffc;color:#2e6e9e}\
	.swingjsPopupMenu .ui-state-default .a{color:#2e6e9e;}\
	.swingjsPopupMenu .ui-state-hover,.swingjsPopupMenu .ui-state-focus{background:#d0e5f5;color:#1d5987}\
	.swingjsPopupMenu .ui-state-hover .a{color:#1d5987;cursor:pointer;}\
	.swingjsPopupMenu .ui-state-active{border:1px solid #79b7e7;background:#f5f8f9;color:#e17009}\
	.swingjsPopupMenu .ui-state-active .a{color:#e17009;cursor:pointer;}\
	.swingjsPopupMenu .ui-state-highlight{border:1px solid #fad42e;background:#fbec88;color:#363636}\
	.swingjsPopupMenu .ui-state-highlight .a{color:#363636}\
	.swingjsPopupMenu .ui-state-disabled *{color:#d6d6d6!important;font-weight:normal;cursor:default}\
	.swingjsPopupMenu .ui-state-disabled .a:hover{background-color:transparent!important;border-color:transparent!important}\
	.swingjsPopupMenu .ui-state-disabled .ui-icon{filter:Alpha(Opacity=35)}'};

Swing.setMenu = function(menu) {
  // the object will be installed using $(body).after()
  
  // called by javajs.swing.JPopupMenu or swingjs.plaf.JSPopupMenuUI
  menu._applet = (menu.ui ? menu.ui.applet : menu.applet); // SwingJS vs JSmol
	Swing.__getMenuStyle && J2S.$after("head", '<style>'+Swing.__getMenuStyle(menu._applet)+'</style>');  
	Swing.__getMenuStyle = null; // "static"
	  
	// TODO: We can't be creating fields in JPopupMenu! This is ancient stuff.
	  
    menu._visible = false;
    menu._j2sname = menu.id = menu.ui.id + '_' + (++Swing.menuCounter);
    menu.$ulTop = J2S.__$(); // empty jQuery selector
    var proto = menu.$init$.exClazz["prototype"];
    proto._hideJSMenu = function(){Swing.hideMenu(this)};
    proto.dragBind || ( proto.dragBind = function(isBind){} );
    proto.setContainer || ( proto.setContainer = function(c){ this.$ulTop = c } );
    proto.setPosition || ( proto.setPosition = function(x,y) {
      this.$ulTop.css({left:x+"px",top:y+"px",position:"absolute"});
    } );    
	menu._applet._menus || (menu._applet._menus = {});
	menu._applet._menus[menu._j2sname] = menu;
	menu._tainted = true;
}

Swing.updateMenu = function(menu, andShow) {
	    // for SwingJS the top node is domNode itself, which is already <ul>
    var node = menu.ui.domNode;
    if (node != menu.$ulTop[0]) {
        if (menu.$ulTop) {
          menu.$ulTop.remove();
        }
        menu.setContainer(J2S.$(node));
        J2S.$(node).addClass("swingjsPopupMenu");
    }
	node.style.display = (andShow ? "block" : "none");
    J2S.$after("body",node);
    var m = menu.$ulTop.j2smenu({delay:100, jPopupMenu: menu});
    m.j2smenu('refresh');  
    // this next is critical for SwingJS
	menu._tainted = false;
}

Swing.getInstance = function(menu) {
	return menu.$ulTop.data("ui-j2smenu");
}

var ensureMouseSet = function(menu, node) {
	// allow mouseup and other events to do their job
	// for all JMenu and JMenuItem entries
    var v = node.find("[role=menuitem]");
    for (var i = v.length; --i >= 0;) {
    	if (v[i]._menu != menu) {
    		setMouseMenuItem(menu, v[i]);
    	}
    }
}

var setMouseMenuItem = function(menu, node) {
    J2S.unsetMouse(node);
    node._menu = menu;
    node.applet = menu._applet;
    while (!node.applet && menu.invoker.parent != null) {
    	menu = menu.invoker.parent;
    node.applet = menu._applet;
    }
    node._frameViewer = menu.invoker.getFrameViewer$();
    J2S.setMouse(node, true);
}

Swing.showMenu = function(menu, x, y) {
  // called by javajs.swing.JPopupMenu and swingjs.plaf.JSPopupMenuUI
  // allow for a user callback for customization of menu
 
  for (var i in menu._applet._menus)
    Swing.hideMenu(menu._applet._menus[i], true);  
  if (J2S._showMenuCallback)
	J2S._showMenuCallback(menu, x, y);
  var wasTainted = menu._tainted;
  
  // TODO: We can't be creating fields in JPopupMenu!
  
  if (menu._tainted)
	  Swing.updateMenu(menu);
  ensureMouseSet(menu, menu.$ulTop);
  menu.setPosition(x, y);
  menu.$ulTop.hide().j2smenu("setClickOut").show();  
  menu._visible = true;
  menu.timestamp = System.currentTimeMillis$();
  menu.dragBind(true);
  menu.$ulTop.bind("contextmenu", function() {return false;});
} 

Swing.hideMenu = function(menu, force) {
  // called internally often -- even on mouse moves
	if (menu._visible === false && !force) return;
	menu.dragBind(false);
	menu.$ulTop.hide();
	menu._visible = menu.isDragging = false;
}

Swing.disposeMenu = function(menu) {
  // called by javajs.swing.JPopupMenu
  if (J2S._persistentMenu)
  	return
  Swing.hideMenu(menu);
  menu.$ulTop.j2smenu().destroy && menu.$ulTop.j2smenu().destroy();
    var v = menu.$ulTop.find("[role=menuitem]");
    for (var i = v.length; --i >= 0;) {
        v[i].applet = menu.ui.applet;
        J2S.unsetMouse(v[i]);
//??        v[i]._frameViewer = null;
//??        v[i]._menu = null;
    }
	delete menu._applet._menus[menu._j2sname];
}


};

})(J2S.Swing, J2S.__$);


// end of j2sMenu.js 2024.01.16 2023.06.04 2020.06.09 2020.05.15  2020.01.25
