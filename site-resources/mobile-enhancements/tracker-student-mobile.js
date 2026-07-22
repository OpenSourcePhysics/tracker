(function (global) {
	"use strict";

	var app = null;
	var resizeQueued = false;
	var popupClampQueued = false;
	var popupObserver = null;
	var popupWatching = false;
	var popupPoller = null;
	var libraryDoubleClickBound = false;
	var libraryTreeBridgeBound = false;
	var windowLayoutQueued = false;
	var touchMouseBridgeBound = false;
	var activeTouchPointer = null;
	var comboTouchBridgeBound = false;
	var activeComboTouch = null;
	var suppressComboClickUntil = 0;
	var nativeMenuSuppressionUntil = 0;
	var mainToolbarShell = null;

	function byId(id) {
		return document.getElementById(id);
	}

	function showFatalError(message) {
		var loading = byId("loading-message");
		if (loading) loading.textContent = message;
	}

	function legacyFrame() {
		if (!app) return null;
		if (typeof app.getMainFrame === "function") return app.getMainFrame();
		if (typeof app.getMainFrame$ === "function") return app.getMainFrame$();
		return null;
	}

	function normalizedJavaLabel(component) {
		if (!component || typeof component.getText$ !== "function") return "";
		return (component.getText$() || "").replace(/\u00a0/g, " ").trim();
	}

	function javaMenuChildren(component) {
		var values = null;
		if (component && typeof component.getMenuComponents$ === "function") {
			values = component.getMenuComponents$();
		}
		if ((!values || !values.length) && component && typeof component.getComponents$ === "function") {
			values = component.getComponents$();
		}
		return values ? Array.prototype.slice.call(values).filter(Boolean) : [];
	}

	function initializeJavaMenu(component) {
		if (!component || typeof component.getPopupMenu$ !== "function") return;
		var usefulChildren = javaMenuChildren(component).some(function (child) {
			return Boolean(normalizedJavaLabel(child));
		});
		if (!usefulChildren && typeof component.doClick$ === "function") {
			nativeMenuSuppressionUntil = Date.now() + 900;
			component.doClick$();
			if (typeof component.setPopupMenuVisible$Z === "function") component.setPopupMenuVisible$Z(false);
			global.setTimeout(function () {
				if (typeof component.setPopupMenuVisible$Z === "function") component.setPopupMenuVisible$Z(false);
				suppressNativeSwingMenus();
			}, 0);
		}
	}

	function suppressNativeSwingMenus() {
		if (Date.now() > nativeMenuSuppressionUntil) return;
		document.querySelectorAll(".swingjsPopupMenu").forEach(function (popup) {
			popup.classList.add("tracker-mobile-native-menu-suppressed");
		});
	}

	function mainJavaMenuBar() {
		var frame = legacyFrame();
		return frame && typeof frame.getJMenuBar$ === "function" ? frame.getJMenuBar$() : null;
	}

	function findJavaMenuPath(path) {
		var current = mainJavaMenuBar();
		for (var i = 0; current && i < path.length; i += 1) {
			var label = path[i];
			initializeJavaMenu(current);
			current = javaMenuChildren(current).find(function (child) {
				return normalizedJavaLabel(child) === label;
			});
		}
		return current || null;
	}

	function activateJavaComponent(component) {
		if (!component) return false;
		if (typeof component.isEnabled$ === "function" && !component.isEnabled$()) return false;
		if (typeof component.doClick$ === "function") component.doClick$();
		else if (typeof component.doClick$I === "function") component.doClick$I(0);
		else return false;
		return true;
	}

	function invokeJavaMenuPath(path) {
		return activateJavaComponent(findJavaMenuPath(path));
	}

	function inlinePixels(element, property, fallback) {
		if (!element || !element.style) return fallback;
		var value = parseFloat(element.style.getPropertyValue(property));
		return isFinite(value) ? value : fallback;
	}

	function compactTouchUI(width) {
		var coarse = typeof global.matchMedia === "function" && global.matchMedia("(pointer: coarse)").matches;
		return width <= 820 || (coarse && width <= 1180);
	}

	function setToolbarVariable(element, name, value) {
		if (!element || !element.style || element.style.getPropertyValue(name) === value) return;
		element.style.setProperty(name, value);
	}

	function bindNativeSheetButton(button, activate) {
		var lastActivation = 0;
		function suppress(event) {
			event.preventDefault();
			event.stopImmediatePropagation();
		}
		function release(event) {
			suppress(event);
			var now = Date.now();
			if (now - lastActivation < 500) return;
			lastActivation = now;
			global.setTimeout(activate, 0);
		}
		button.addEventListener("pointerdown", suppress, true);
		button.addEventListener("mousedown", suppress, true);
		button.addEventListener("touchstart", suppress, { capture: true, passive: false });
		button.addEventListener("pointerup", release, true);
		button.addEventListener("mouseup", release, true);
		button.addEventListener("touchend", release, { capture: true, passive: false });
		button.addEventListener("click", suppress, true);
	}

	function createNativeSheet(title, onBack) {
		var oldSheet = document.querySelector(".tracker-mobile-submenu-sheet");
		if (oldSheet) oldSheet.remove();
		var sheet = document.createElement("div");
		sheet.className = "tracker-mobile-submenu-sheet tracker-mobile-command-sheet";
		sheet.setAttribute("role", "menu");
		sheet.setAttribute("aria-label", title);
		var heading = document.createElement("div");
		heading.className = "tracker-mobile-submenu-heading";
		var headingText = document.createElement("strong");
		headingText.textContent = title;
		heading.appendChild(headingText);
		var headingActions = document.createElement("span");
		headingActions.className = "tracker-mobile-sheet-heading-actions";
		if (onBack) {
			var back = document.createElement("button");
			back.type = "button";
			back.textContent = "Back";
			back.addEventListener("click", onBack);
			headingActions.appendChild(back);
		}
		var close = document.createElement("button");
		close.type = "button";
		close.textContent = "Close";
		close.addEventListener("click", function () { sheet.remove(); });
		headingActions.appendChild(close);
		heading.appendChild(headingActions);
		sheet.appendChild(heading);
		document.body.appendChild(sheet);
		return sheet;
	}

	function comboForWrapper(wrapper) {
		if (!wrapper) return null;
		var ui = wrapper["data-ui"] || (wrapper["data-component"] && wrapper["data-component"].ui);
		if (ui && ui.comboBox) return ui.comboBox;
		var list = wrapper["data-shadowkeycomponent"];
		return list && list.cbui ? list.cbui.comboBox : null;
	}

	function comboItemLabel(item, index) {
		if (item == null) return "Option " + (index + 1);
		var value = "";
		if (typeof item.getName$ === "function") value = item.getName$() || "";
		if (!value && typeof item.getText$ === "function") value = item.getText$() || "";
		if (!value && typeof item.toString$ === "function") value = item.toString$() || "";
		if (!value && typeof item.length === "number") {
			for (var partIndex = item.length - 1; partIndex >= 0 && !value; partIndex -= 1) {
				var part = item[partIndex];
				if (typeof part === "string") value = part;
				else if (part && typeof part.getName$ === "function") value = part.getName$() || "";
				else if (part && typeof part.getText$ === "function") value = part.getText$() || "";
			}
		}
		if (!value) value = String(item);
		value = value.replace(/\u00a0/g, " ").trim();
		return value && value !== "[object Object]" ? value : "Option " + (index + 1);
	}

	function hideNativeComboPopup(combo) {
		if (!combo) return;
		if (typeof combo.setPopupVisible$Z === "function") combo.setPopupVisible$Z(false);
		else if (typeof combo.hidePopup$ === "function") combo.hidePopup$();
	}

	function showTouchComboSheet(wrapper) {
		var combo = comboForWrapper(wrapper);
		if (!combo || typeof combo.getItemCount$ !== "function" ||
			typeof combo.getItemAt$I !== "function") return false;
		if (typeof combo.isEnabled$ === "function" && !combo.isEnabled$()) return false;
		var count = combo.getItemCount$();
		if (!count) return false;
		hideNativeComboPopup(combo);
		var selected = typeof combo.getSelectedIndex$ === "function" ? combo.getSelectedIndex$() : -1;
		var current = selected >= 0 ? comboItemLabel(combo.getItemAt$I(selected), selected) : "";
		var sheet = createNativeSheet(current ? "Choose: " + current : "Choose an option");
		sheet.classList.add("tracker-mobile-combo-sheet");
		sheet.setAttribute("role", "listbox");
		for (var i = 0; i < count; i += 1) {
			(function (index) {
				var button = document.createElement("button");
				button.type = "button";
				button.textContent = comboItemLabel(combo.getItemAt$I(index), index);
				button.setAttribute("role", "option");
				button.setAttribute("aria-selected", index === selected ? "true" : "false");
				bindNativeSheetButton(button, function () {
					hideNativeComboPopup(combo);
					if (typeof combo.setSelectedIndex$I === "function") combo.setSelectedIndex$I(index);
					sheet.remove();
					queueResize();
				});
				sheet.appendChild(button);
			})(i);
		}
		var selectedButton = sheet.querySelector('[aria-selected="true"]');
		if (selectedButton && typeof selectedButton.scrollIntoView === "function") {
			selectedButton.scrollIntoView({ block: "nearest" });
		}
		return true;
	}

	function comboWrapperAt(target) {
		if (!target || typeof target.closest !== "function") return null;
		var candidate = target.closest("div[id^='Tracker_ComboBoxUI_']");
		while (candidate) {
			if (comboForWrapper(candidate)) return candidate;
			candidate = candidate.parentElement;
			if (!candidate || !candidate.matches("div[id^='Tracker_ComboBoxUI_']")) return null;
		}
		return null;
	}

	function bindComboTouchBridge() {
		if (comboTouchBridgeBound) return;
		comboTouchBridgeBound = true;
		document.addEventListener("pointerdown", function (event) {
			if (event.pointerType !== "touch" || !compactTouchUI(visualViewportRect().width)) return;
			var wrapper = comboWrapperAt(event.target);
			if (!wrapper || !comboForWrapper(wrapper)) return;
			event.preventDefault();
			event.stopImmediatePropagation();
			activeComboTouch = { pointerId: event.pointerId, wrapper: wrapper };
		}, true);
		document.addEventListener("pointerup", function (event) {
			if (!activeComboTouch || event.pointerId !== activeComboTouch.pointerId) return;
			event.preventDefault();
			event.stopImmediatePropagation();
			var wrapper = activeComboTouch.wrapper;
			activeComboTouch = null;
			suppressComboClickUntil = Date.now() + 700;
			global.setTimeout(function () { showTouchComboSheet(wrapper); }, 0);
		}, true);
		document.addEventListener("pointercancel", function (event) {
			if (activeComboTouch && event.pointerId === activeComboTouch.pointerId) activeComboTouch = null;
		}, true);
		document.addEventListener("click", function (event) {
			var wrapper = comboWrapperAt(event.target);
			if (!wrapper || !compactTouchUI(visualViewportRect().width)) return;
			event.preventDefault();
			event.stopImmediatePropagation();
			if (Date.now() <= suppressComboClickUntil) return;
			showTouchComboSheet(wrapper);
		}, true);
	}

	function addNativeSheetAction(sheet, label, activate, nested, disabled) {
		var button = document.createElement("button");
		button.type = "button";
		button.textContent = label + (nested ? " \u203a" : "");
		button.setAttribute("role", "menuitem");
		button.disabled = Boolean(disabled);
		bindNativeSheetButton(button, activate);
		sheet.appendChild(button);
	}

	function showJavaMenuSheet(title, component, parent) {
		if (!component) return;
		initializeJavaMenu(component);
		var sheet = createNativeSheet(title, parent ? function () {
			showJavaMenuSheet(parent.title, parent.component, parent.parent);
		} : null);
		javaMenuChildren(component).forEach(function (child) {
			var label = normalizedJavaLabel(child);
			if (!label) return;
			var nested = javaMenuChildren(child).length > 0 || typeof child.getPopupMenu$ === "function";
			var disabled = typeof child.isEnabled$ === "function" && !child.isEnabled$();
			addNativeSheetAction(sheet, label, function () {
				if (nested) {
					showJavaMenuSheet(label, child, { title: title, component: component, parent: parent });
					return;
				}
				activateJavaComponent(child);
				nativeMenuSuppressionUntil = Date.now() + 900;
				suppressNativeSwingMenus();
				sheet.remove();
				global.setTimeout(updateMobileCommandBarState, 80);
			}, nested, disabled);
		});
	}

	function showMeasureSheet() {
		var actions = [
			["Calibration Stick", ["Track", "New", "Calibration Tools", "Calibration Stick"]],
			["Calibration Points", ["Track", "New", "Calibration Tools", "Calibration Points"]],
			["Tape Measure", ["Track", "New", "Measuring Tools", "Tape Measure"]],
			["Protractor", ["Track", "New", "Measuring Tools", "Protractor"]],
			["Circle Fitter", ["Track", "New", "Measuring Tools", "Circle Fitter"]],
			["Offset Origin", ["Track", "New", "Calibration Tools", "Offset Origin"]]
		];
		var sheet = createNativeSheet("Measure and calibrate");
		actions.forEach(function (entry) {
			var component = findJavaMenuPath(entry[1]);
			var disabled = !component || (typeof component.isEnabled$ === "function" && !component.isEnabled$());
			addNativeSheetAction(sheet, entry[0], function () {
				invokeJavaMenuPath(entry[1]);
				nativeMenuSuppressionUntil = Date.now() + 900;
				suppressNativeSwingMenus();
				sheet.remove();
				global.setTimeout(updateMobileCommandBarState, 80);
			}, false, disabled);
		});
	}

	function selectedTrackerPanel() {
		var frame = legacyFrame();
		return frame && typeof frame.getSelectedPanel$ === "function" ? frame.getSelectedPanel$() : null;
	}

	function toggleMobileAxes() {
		var panel = selectedTrackerPanel();
		if (panel && typeof panel.toggleAxesVisible$ === "function") panel.toggleAxesVisible$();
		global.setTimeout(updateMobileCommandBarState, 40);
	}

	function setMobileWorkspaceView(viewName) {
		var frame = legacyFrame();
		var panel = selectedTrackerPanel();
		if (!frame || !panel) return false;
		if (app && typeof app.showMobileView === "function") {
			return Boolean(app.showMobileView(viewName));
		}
		var viewIndex = { video: 4, plot: 0, table: 1 }[viewName];
		if (viewName === "all") {
			if (typeof panel.restoreViews$ === "function") panel.restoreViews$();
			else if (typeof frame.restoreViews$org_opensourcephysics_cabrillo_tracker_TrackerPanel === "function") {
				frame.restoreViews$org_opensourcephysics_cabrillo_tracker_TrackerPanel(panel);
			} else return false;
			return true;
		}
		if (typeof viewIndex !== "number") return false;
		var maximize = frame.maximizeView$org_opensourcephysics_cabrillo_tracker_TrackerPanel$I;
		if (typeof maximize !== "function") return false;
		maximize.call(frame, panel, viewIndex);
		return true;
	}

	function showWorkspaceViewSheet() {
		var sheet = createNativeSheet("Choose workspace view");
		[
			["Video and marking", "video"],
			["Plots and graphs", "plot"],
			["Data table", "table"],
			["All panels", "all"]
		].forEach(function (entry) {
			addNativeSheetAction(sheet, entry[0], function () {
				setMobileWorkspaceView(entry[1]);
				sheet.remove();
				global.setTimeout(function () {
					enhanceMainToolbar();
					markTouchInteractionSurfaces();
					queueResize();
				}, 80);
			}, false, false);
		});
		addNativeSheetAction(sheet, "Show or hide coordinate axes", function () {
			toggleMobileAxes();
			sheet.remove();
		}, false, false);
	}

	function updateMobileCommandBarState() {
		var panel = selectedTrackerPanel();
		var axes = panel && typeof panel.getAxes$ === "function" ? panel.getAxes$() : null;
		var visible = Boolean(axes && typeof axes.isVisible$ === "function" && axes.isVisible$());
		document.querySelectorAll(".tracker-mobile-commandbar [data-mobile-command='axes']").forEach(function (button) {
			button.setAttribute("aria-pressed", String(visible));
			button.classList.toggle("is-active", visible);
			button.textContent = visible ? "Axes on" : "Axes";
		});
	}

	function showMobileCommandGroup(group) {
		var menuBar = mainJavaMenuBar();
		if (!menuBar) return;
		if (group === "project") showJavaMenuSheet("Project", findJavaMenuPath(["File"]));
		else if (group === "track") showJavaMenuSheet("New track", findJavaMenuPath(["Track", "New"]));
		else if (group === "measure") showMeasureSheet();
		else if (group === "views") showWorkspaceViewSheet();
		else if (group === "more") showJavaMenuSheet("All Tracker menus", menuBar);
	}

	function ensureMobileCommandBar(shell) {
		if (!shell) return;
		var menuBars = Array.prototype.filter.call(
			document.querySelectorAll("div[id^='Tracker_MenuBarUI_'][id$='div']"),
			function (bar) {
				var rect = bar.getBoundingClientRect();
				return rect.width > 280 && rect.top < 90 && !bar.closest(".tracker-mobile-responsive-window");
			}
		);
		menuBars.forEach(function (bar) { bar.classList.add("tracker-mobile-main-menubar"); });
		var allCommandBars = Array.prototype.slice.call(document.querySelectorAll(".tracker-mobile-commandbar"));
		var commandbar = shell.querySelector(":scope > .tracker-mobile-commandbar") || allCommandBars[0];
		allCommandBars.forEach(function (bar) {
			if (bar !== commandbar) bar.remove();
		});
		if (!commandbar) {
			commandbar = document.createElement("nav");
			commandbar.className = "tracker-mobile-commandbar";
			commandbar.setAttribute("aria-label", "Tracker mobile commands");
			[
				["project", "Project"], ["track", "New track"], ["measure", "Measure"],
				["views", "Views"], ["more", "More"]
			].forEach(function (entry) {
				var button = document.createElement("button");
				button.type = "button";
				button.setAttribute("data-mobile-command", entry[0]);
				button.textContent = entry[1];
				bindNativeSheetButton(button, function () { showMobileCommandGroup(entry[0]); });
				commandbar.appendChild(button);
			});
		}
		if (commandbar.parentElement !== shell) shell.appendChild(commandbar);
		commandbar.classList.remove("tracker-mobile-toolbar");
		updateMobileCommandBarState();
	}

	function mainToolbar() {
		if (mainToolbarShell && mainToolbarShell.isConnected) return mainToolbarShell;
		mainToolbarShell = null;
		var candidates = Array.prototype.filter.call(
			document.querySelectorAll("div[id^='Tracker_ToolBarUI_'][id$='div']"),
			function (element) {
				var rect = element.getBoundingClientRect();
				return rect.width > 300 && rect.height > 20 && rect.top < 150;
			}
		);
		candidates.sort(function (a, b) {
			return a.getBoundingClientRect().top - b.getBoundingClientRect().top;
		});
		mainToolbarShell = candidates[0] || null;
		return mainToolbarShell;
	}

	function enhanceMainToolbar() {
		var shell = mainToolbar();
		var toolbar = shell && Array.prototype.find.call(shell.children, function (child) {
			return !child.classList.contains("tracker-mobile-commandbar");
		});
		if (!toolbar) return;
		shell.classList.add("tracker-mobile-toolbar-shell");
		toolbar.classList.add("tracker-mobile-toolbar");
		ensureMobileCommandBar(shell);

		var toolbarWidth = shell.clientWidth || toolbar.getBoundingClientRect().width;
		var compact = compactTouchUI(viewportSize().width);
		var controls = Array.prototype.map.call(toolbar.children, function (control) {
			var rect = control.getBoundingClientRect();
			var button = control.querySelector(".j2sbutton");
			/* Read SwingJS's inline geometry on every pass. It may finish laying
			 * out text controls after our first poll, while our CSS only overrides
			 * the rendered geometry through custom properties. */
			var baseWidth = inlinePixels(control, "width", button ? rect.width : 0);
			var baseLeft = inlinePixels(control, "left", rect.left - toolbar.getBoundingClientRect().left);
			var canvas = button && button.querySelector("canvas");
			var label = button && button.querySelector("[id$='_txt']");
			var text = label ? label.textContent.replace(/\u00a0/g, " ").trim() : "";
			var canvasWidth = canvas ? parseFloat(canvas.getAttribute("width")) : 0;
			var separator = canvasWidth > 0 && canvasWidth <= 2;
			var targetWidth = baseWidth;
			if (!button) targetWidth = baseWidth;
			else if (separator) targetWidth = 14;
			else if (text) targetWidth = baseWidth + 14;
			else if (baseWidth <= 18) targetWidth = 27;
			else if (baseWidth <= 24) targetWidth = 34;
			else if (baseWidth <= 36) targetWidth = 46;
			else if (button) targetWidth = baseWidth + 10;

			control.classList.add("tracker-toolbar-control");
			control.classList.toggle("tracker-toolbar-separator", separator);
			control.classList.toggle("tracker-toolbar-text-control", Boolean(text));
			control.classList.toggle("tracker-toolbar-icon-only", Boolean(button && !text && !separator));
			var rightGroup = baseLeft > toolbarWidth * 0.65 ? "right" : "left";
			return { element: control, width: targetWidth, right: rightGroup === "right" };
		});

		var cursor = 2;
		controls.filter(function (item) { return !item.right; }).forEach(function (item) {
			setToolbarVariable(item.element, "--tracker-control-left", Math.round(cursor) + "px");
			setToolbarVariable(item.element, "--tracker-control-width", Math.round(item.width) + "px");
			cursor += item.width;
		});

		if (compact) {
			controls.filter(function (item) { return item.right; }).forEach(function (item) {
				setToolbarVariable(item.element, "--tracker-control-left", Math.round(cursor) + "px");
				setToolbarVariable(item.element, "--tracker-control-width", Math.round(item.width) + "px");
				cursor += item.width;
			});
			setToolbarVariable(toolbar, "--tracker-toolbar-content-width", Math.ceil(cursor + 2) + "px");
		} else {
			var rightEdge = toolbarWidth - 2;
			controls.filter(function (item) { return item.right; }).reverse().forEach(function (item) {
				rightEdge -= item.width;
				setToolbarVariable(item.element, "--tracker-control-left", Math.round(rightEdge) + "px");
				setToolbarVariable(item.element, "--tracker-control-width", Math.round(item.width) + "px");
			});
			setToolbarVariable(toolbar, "--tracker-toolbar-content-width", Math.ceil(toolbarWidth) + "px");
		}
	}

	function clampPopupMenus() {
		popupClampQueued = false;
		bindSubmenuTriggers();
		bindNestedMenuItems();
		var margin = 8;
		var visibleViewport = visualViewportRect();
		var compact = compactTouchUI(visibleViewport.width);
		document.querySelectorAll(".swingjsPopupMenu").forEach(function (popup) {
			if (global.getComputedStyle(popup).display === "none") return;
			var rect = popup.getBoundingClientRect();
			if (!rect.width || !rect.height) return;
			var left = rect.left;
			popup.style.setProperty("--tracker-popup-max-height",
				Math.max(120, visibleViewport.height - margin * 2) + "px");
			if (rect.right > visibleViewport.left + visibleViewport.width - margin) {
				left -= rect.right - (visibleViewport.left + visibleViewport.width - margin);
			}
			if (left < visibleViewport.left + margin) left = visibleViewport.left + margin;
			if (Math.abs(left - rect.left) > 1) {
				popup.style.left = Math.round(left) + "px";
				popup.style.right = "auto";
			}
			var submenus = popup.querySelectorAll("ul.ui-j2smenu");
			var submenuOpen = Array.prototype.some.call(submenus, function (submenu) {
				return global.getComputedStyle(submenu).display !== "none";
			});
			popup.classList.toggle("tracker-popup-submenu-open", submenuOpen);
			if (!compact) return;
			submenus.forEach(function (submenu) {
				if (global.getComputedStyle(submenu).display === "none") return;
				if (!submenu.getAttribute("data-tracker-logical-menu-position")) {
					submenu.setAttribute("data-tracker-logical-menu-position", [
						inlinePixels(submenu, "left", 0), inlinePixels(submenu, "top", 0)
					].join(","));
				}
				var parentItem = submenu.closest("li.ui-j2smenu-item");
				var parentRect = parentItem ? parentItem.getBoundingClientRect() : popup.getBoundingClientRect();
				var submenuRect = submenu.getBoundingClientRect();
				var maxHeight = Math.max(120, visibleViewport.height - margin * 2);
				submenu.style.setProperty("max-height", maxHeight + "px", "important");
				submenu.style.setProperty("left", (visibleViewport.left + margin) + "px", "important");
				submenu.style.setProperty("width", Math.max(240, visibleViewport.width - margin * 2) + "px", "important");
				var effectiveHeight = Math.min(submenuRect.height, maxHeight);
				var top = Math.max(visibleViewport.top + margin, Math.min(parentRect.top,
					visibleViewport.top + visibleViewport.height - effectiveHeight - margin));
				var topValue = Math.round(top) + "px";
				if (submenu.style.getPropertyValue("top") !== topValue ||
					submenu.style.getPropertyPriority("top") !== "important") {
					submenu.style.setProperty("top", topValue, "important");
				}
			});
		});
	}

	function submenuTrigger(target) {
		if (!target || typeof target.closest !== "function") return null;
		return target.closest(".swingjsPopupMenu li.ui-j2smenu-item > .a[aria-haspopup='true']");
	}

	function openSubmenuForTap(trigger) {
		[trigger.parentElement, trigger].forEach(function (element) {
			if (!element) return;
			element.dispatchEvent(new MouseEvent("mouseover", {
				bubbles: true,
				cancelable: true,
				view: global
			}));
		});
		var submenu = trigger.parentElement && trigger.parentElement.querySelector(":scope > ul.ui-j2smenu");
		if (submenu) {
			/* On a compact touch viewport use the native sheet exclusively. Moving
			 * the SwingJS submenu under the finger during pointerdown can make the
			 * matching pointerup/click activate its first row by accident. */
			if (compactTouchUI(visualViewportRect().width)) {
				submenu.style.setProperty("display", "none", "important");
				submenu.setAttribute("aria-hidden", "true");
				submenu.setAttribute("aria-expanded", "false");
				trigger.setAttribute("aria-expanded", "true");
				showTouchSubmenuSheet(trigger, submenu);
				return;
			}
			function revealSubmenu() {
				submenu.style.setProperty("display", "block", "important");
				submenu.setAttribute("aria-hidden", "false");
				submenu.setAttribute("aria-expanded", "true");
				trigger.setAttribute("aria-expanded", "true");
			}
			revealSubmenu();
			/* SwingJS may process the matching touchend after our pointerdown and
			 * hide the hover submenu again. Reassert the requested touch state after
			 * that compatibility event has completed. */
			global.setTimeout(function () { revealSubmenu(); queuePopupClamp(); }, 60);
			global.setTimeout(function () { revealSubmenu(); queuePopupClamp(); }, 160);
		}
		queuePopupClamp();
		global.setTimeout(queuePopupClamp, 40);
	}

	function handleSubmenuPointer(event) {
		var overlay = event.target && typeof event.target.closest === "function" ?
			event.target.closest(".tracker-submenu-tap-target") : null;
		var trigger = overlay ? byId(overlay.getAttribute("data-trigger-id")) : submenuTrigger(event.target);
		if (!trigger) return;
		event.preventDefault();
		event.stopImmediatePropagation();
		openSubmenuForTap(trigger);
	}

	function handleSubmenuClick(event) {
		var overlay = event.target && typeof event.target.closest === "function" ?
			event.target.closest(".tracker-submenu-tap-target") : null;
		var trigger = overlay ? byId(overlay.getAttribute("data-trigger-id")) : submenuTrigger(event.target);
		if (!trigger) return;
		event.preventDefault();
		event.stopImmediatePropagation();
		openSubmenuForTap(trigger);
	}

	function bindSubmenuTriggers() {
		Array.prototype.forEach.call(
			document.querySelectorAll(".swingjsPopupMenu li.ui-j2smenu-item > .a[aria-haspopup='true']"),
			function (trigger) {
				if (trigger.getAttribute("data-tracker-touch-menu") === "true") return;
				var item = trigger.parentElement;
				trigger.setAttribute("data-tracker-touch-menu", "true");
				var tapTarget = document.createElement("span");
				tapTarget.className = "tracker-submenu-tap-target";
				tapTarget.setAttribute("aria-hidden", "true");
				tapTarget.setAttribute("data-trigger-id", trigger.id);
				item.appendChild(tapTarget);
				var lastActivation = 0;
				function suppress(event) {
					event.preventDefault();
					event.stopImmediatePropagation();
				}
				function activateOnRelease(event) {
					suppress(event);
					var now = Date.now();
					if (now - lastActivation < 500) return;
					lastActivation = now;
					/* Defer until the release event finishes so the new sheet cannot become
					 * the target of the same physical tap. */
					global.setTimeout(function () { openSubmenuForTap(trigger); }, 0);
				}
				[tapTarget, trigger].forEach(function (target) {
					target.addEventListener("pointerdown", suppress, true);
					target.addEventListener("mousedown", suppress, true);
					target.addEventListener("touchstart", suppress, { capture: true, passive: false });
					target.addEventListener("pointerup", activateOnRelease, true);
					target.addEventListener("mouseup", activateOnRelease, true);
					target.addEventListener("touchend", activateOnRelease, { capture: true, passive: false });
					target.addEventListener("click", suppress, true);
				});
			}
		);
	}

	function bindNestedMenuItems() {
		Array.prototype.forEach.call(
			document.querySelectorAll("ul.ui-j2smenu > li.ui-j2smenu-item"),
			function (item) {
				if (item.getAttribute("data-tracker-menu-action") === "true") return;
				var action = item.querySelector(":scope > .a");
				if (!action || action.getAttribute("aria-haspopup") === "true") return;
				item.setAttribute("data-tracker-menu-action", "true");
				var lastActivation = 0;
				function activate(event) {
					if (event.type === "pointerdown" && event.pointerType && event.pointerType !== "touch") return;
					var now = Date.now();
					event.preventDefault();
					event.stopImmediatePropagation();
					if (now - lastActivation < 500) return;
					lastActivation = now;
					dispatchLogicalMenuClick(item, action);
				}
				item.addEventListener("pointerdown", activate, true);
				item.addEventListener("touchstart", activate, { capture: true, passive: false });
			}
		);
	}

	function dispatchLogicalMenuClick(item, action) {
		var menu = item.parentElement;
		var items = Array.prototype.filter.call(menu.children, function (child) {
			return child.matches("li.ui-j2smenu-item");
		});
		var index = Math.max(0, items.indexOf(item));
		var row = item.querySelector("[id$='_ctr']");
		var rowHeight = inlinePixels(row, "height", 22);
		var savedPosition = (menu.getAttribute("data-tracker-logical-menu-position") || "").split(",");
		var logicalLeft = savedPosition.length === 2 ? parseFloat(savedPosition[0]) :
			inlinePixels(menu, "left", menu.getBoundingClientRect().left);
		var logicalTop = savedPosition.length === 2 ? parseFloat(savedPosition[1]) :
			inlinePixels(menu, "top", menu.getBoundingClientRect().top);
		var logicalWidth = Math.max(40, inlinePixels(action, "min-width", 160));
		var clickEvent = new MouseEvent("click", {
			bubbles: true,
			cancelable: true,
			view: global,
			clientX: logicalLeft + logicalWidth / 2,
			clientY: logicalTop + index * rowHeight + rowHeight / 2
		});
		Object.defineProperty(clickEvent, "trackerLogicalCoordinates", { value: true });
		action.dispatchEvent(clickEvent);
	}

	function showTouchSubmenuSheet(trigger, submenu) {
		var oldSheet = document.querySelector(".tracker-mobile-submenu-sheet");
		if (oldSheet) oldSheet.remove();
		var sheet = document.createElement("div");
		sheet.className = "tracker-mobile-submenu-sheet";
		sheet.setAttribute("role", "menu");
		sheet.setAttribute("aria-label", trigger.textContent.replace(/\u00a0/g, " ").trim());
		var heading = document.createElement("div");
		heading.className = "tracker-mobile-submenu-heading";
		var headingText = document.createElement("strong");
		headingText.textContent = sheet.getAttribute("aria-label");
		var close = document.createElement("button");
		close.type = "button";
		close.textContent = "Close";
		close.addEventListener("click", function () { sheet.remove(); });
		heading.appendChild(headingText);
		heading.appendChild(close);
		sheet.appendChild(heading);

		Array.prototype.forEach.call(submenu.children, function (item) {
			if (!item.matches("li.ui-j2smenu-item")) return;
			var label = item.querySelector(":scope > .a [id$='_txt']");
			var text = label ? label.textContent.replace(/\u00a0/g, " ").trim() : "";
			if (!text) return;
			var action = item.querySelector(":scope > .a");
			var button = document.createElement("button");
			button.type = "button";
			var nested = item.querySelector(":scope > ul.ui-j2smenu");
			button.textContent = text + (nested ? " \u203a" : "");
			button.setAttribute("role", "menuitem");
			var lastActivation = 0;
			function suppress(event) {
				event.preventDefault();
				event.stopImmediatePropagation();
			}
			function activateOnRelease(event) {
				suppress(event);
				var now = Date.now();
				if (now - lastActivation < 500) return;
				lastActivation = now;
				/* Keep the sheet in place through pointerup and click. Removing it on
				 * pointerdown exposes the application underneath to the same tap. */
				global.setTimeout(function () {
					if (nested) {
						showTouchSubmenuSheet(action, nested);
						return;
					}
					if (!invokeJavaMenuItem(text)) {
						if (action) dispatchLogicalMenuClick(item, action);
					}
					sheet.remove();
				}, 0);
			}
			button.addEventListener("pointerdown", suppress, true);
			button.addEventListener("mousedown", suppress, true);
			button.addEventListener("touchstart", suppress, { capture: true, passive: false });
			button.addEventListener("pointerup", activateOnRelease, true);
			button.addEventListener("mouseup", activateOnRelease, true);
			button.addEventListener("touchend", activateOnRelease, { capture: true, passive: false });
			button.addEventListener("click", suppress, true);
			sheet.appendChild(button);
		});
		document.body.appendChild(sheet);
	}

	function invokeJavaMenuItem(label) {
		var frame = legacyFrame();
		if (!frame) return false;
		var roots = [];
		var browser = frame.libraryBrowser;
		if (browser) roots.push(browser.fileMenu, browser.collectionsMenu, browser.helpMenu);
		var menuBar = typeof frame.getJMenuBar$ === "function" ? frame.getJMenuBar$() : null;
		if (menuBar) roots.push(menuBar);
		function children(component) {
			var values = null;
			if (component && typeof component.getMenuComponents$ === "function") values = component.getMenuComponents$();
			else if (component && typeof component.getComponents$ === "function") values = component.getComponents$();
			return values ? Array.prototype.slice.call(values) : [];
		}
		function find(component) {
			if (!component) return null;
			var text = typeof component.getText$ === "function" ? component.getText$() : "";
			if ((text || "").replace(/\u00a0/g, " ").trim() === label) return component;
			var nested = children(component);
			for (var i = 0; i < nested.length; i++) {
				var match = find(nested[i]);
				if (match) return match;
			}
			return null;
		}
		for (var i = 0; i < roots.length; i++) {
			var item = find(roots[i]);
			if (!item) continue;
			if (typeof item.doClick$ === "function") item.doClick$();
			else if (typeof item.doClick$I === "function") item.doClick$I(0);
			else return false;
			return true;
		}
		return false;
	}

	function queuePopupClamp() {
		if (popupClampQueued) return;
		popupClampQueued = true;
		global.requestAnimationFrame(clampPopupMenus);
	}

	function visualViewportRect() {
		var viewport = global.visualViewport;
		return {
			left: Math.round(viewport ? viewport.offsetLeft : 0),
			top: Math.round(viewport ? viewport.offsetTop : 0),
			width: Math.max(1, Math.floor(viewport ? viewport.width : global.innerWidth)),
			height: Math.max(1, Math.floor(viewport ? viewport.height : global.innerHeight))
		};
	}

	function swingWindowTitle(swingWindow) {
		var titleBar = swingWindow && Array.prototype.find.call(swingWindow.children, function (element) {
			return /_titlebar$/.test(element.id);
		});
		var title = titleBar && titleBar.querySelector("[id$='_title']");
		return title ? title.textContent.replace(/\u00a0/g, " ").trim() : "";
	}

	function rememberLogicalBounds(element) {
		if (!element || element.getAttribute("data-tracker-logical-bounds")) return;
		var left = inlinePixels(element, "left", 0);
		var top = inlinePixels(element, "top", 0);
		var width = inlinePixels(element, "width", element.getBoundingClientRect().width);
		var height = inlinePixels(element, "height", element.getBoundingClientRect().height);
		element.setAttribute("data-tracker-logical-bounds", [left, top, width, height].join(","));
	}

	function markRemappedControl(element, className) {
		if (!element) return;
		rememberLogicalBounds(element);
		element.classList.add("tracker-mobile-remapped-control");
		if (className) element.classList.add(className);
		bindRemappedButton(element);
	}

	function bindRemappedButton(wrapper) {
		var button = wrapper && wrapper.querySelector("button");
		if (!button || wrapper.getAttribute("data-tracker-touch-bridge") === "true") return;
		wrapper.setAttribute("data-tracker-touch-bridge", "true");
		var lastActivation = 0;
		function activate(event) {
			if (event.type === "pointerup" && event.pointerType && event.pointerType !== "touch") return;
			var now = Date.now();
			event.preventDefault();
			event.stopImmediatePropagation();
			if (now - lastActivation < 500) return;
			lastActivation = now;
			if (!invokeLibraryJavaAction(wrapper)) dispatchLogicalClick(wrapper, button);
		}
		wrapper.addEventListener("pointerup", activate, true);
		wrapper.addEventListener("touchend", activate, { capture: true, passive: false });
		wrapper.addEventListener("pointerdown", activate, true);
		wrapper.addEventListener("touchstart", activate, { capture: true, passive: false });
	}

	function invokeLibraryJavaAction(wrapper) {
		var frame = legacyFrame();
		var browser = frame && (frame.libraryBrowser ||
			(typeof frame.getLibraryBrowser$ === "function" ? frame.getLibraryBrowser$() : null));
		if (!browser || !wrapper.closest(".tracker-mobile-library-window")) return false;
		var libraryWindow = wrapper.closest(".tracker-mobile-library-window");
		if (wrapper.classList.contains("tracker-library-open")) {
			var commandInput = libraryWindow.querySelector(".tracker-library-url-input input");
			if (commandInput && browser.commandField &&
				typeof browser.commandField.setText$S === "function") {
				browser.commandField.setText$S(commandInput.value);
			}
			if (typeof browser.doCommand$ === "function") browser.doCommand$();
			else if (typeof browser.open$S === "function" && commandInput) browser.open$S(commandInput.value);
			return true;
		}
		if (wrapper.classList.contains("tracker-library-download") &&
			typeof browser.doDownload$ === "function") {
			browser.doDownload$();
			return true;
		}
		if (wrapper.classList.contains("tracker-library-inspect")) {
			var searchInput = libraryWindow.querySelector(".tracker-library-search-input input");
			if (searchInput && browser.searchField &&
				typeof browser.searchField.setText$S === "function") {
				browser.searchField.setText$S(searchInput.value);
			}
			if (browser.searchTargetButton && typeof browser.searchTargetButton.doClick$ === "function") {
				browser.searchTargetButton.doClick$();
			}
			return true;
		}
		var javaButton = wrapper.classList.contains("tracker-library-editor") ? browser.editButton :
			(wrapper.classList.contains("tracker-library-refresh") ? browser.refreshButton : null);
		if (javaButton && typeof javaButton.doClick$ === "function") {
			javaButton.doClick$();
			return true;
		}
		return false;
	}

	function dispatchLogicalClick(wrapper, button) {
		var swingWindow = wrapper.closest(".tracker-mobile-responsive-window");
		var host = swingWindow && swingWindow.parentElement;
		if (!swingWindow || !host) {
			button.click();
			return;
		}
		var saved = (wrapper.getAttribute("data-tracker-logical-bounds") || "0,0,1,1")
			.split(",");
		var logicalLeft = parseFloat(saved[0]) || 0;
		var logicalTop = parseFloat(saved[1]) || 0;
		var logicalWidth = parseFloat(saved[2]) || 1;
		var logicalHeight = parseFloat(saved[3]) || 1;
		var ancestor = wrapper.parentElement;
		while (ancestor && ancestor !== swingWindow) {
			var position = global.getComputedStyle(ancestor).position;
			if (position !== "static") {
				logicalLeft += parseFloat(ancestor.style.left) || 0;
				logicalTop += parseFloat(ancestor.style.top) || 0;
			}
			ancestor = ancestor.parentElement;
		}
		var hostBounds = host.getBoundingClientRect();
		var clickEvent = new MouseEvent("click", {
			bubbles: true,
			cancelable: true,
			view: global,
			clientX: hostBounds.left + logicalLeft + logicalWidth / 2,
			clientY: hostBounds.top + logicalTop + logicalHeight / 2
		});
		Object.defineProperty(clickEvent, "trackerLogicalCoordinates", { value: true });
		button.dispatchEvent(clickEvent);
	}

	function bindLibraryUrlInput(input, openButton) {
		if (!input || !openButton || input.getAttribute("data-tracker-url-bridge") === "true") return;
		input.setAttribute("data-tracker-url-bridge", "true");
		function syncOpenButton() {
			if (input.value.trim()) openButton.disabled = false;
		}
		input.addEventListener("input", syncOpenButton, true);
		input.addEventListener("change", syncOpenButton, true);
		input.addEventListener("keyup", syncOpenButton, true);
	}

	function directComponentWrapper(element) {
		if (!element) return null;
		return element.id && /div$/.test(element.id) ? element : element.parentElement;
	}

	function enhanceLibraryToolbar(swingWindow) {
		var toolbarShell = swingWindow.querySelector("div[id^='Tracker_ToolBarUI_'][id$='div']");
		var toolbar = toolbarShell && toolbarShell.firstElementChild;
		if (!toolbar) return;
		toolbarShell.classList.add("tracker-library-toolbar-shell");
		toolbar.classList.add("tracker-library-toolbar");

		var inputs = toolbar.querySelectorAll("input");
		var buttons = toolbar.querySelectorAll("button");
		var labels = Array.prototype.filter.call(toolbar.querySelectorAll("label"), function (label) {
			return label.textContent.trim() === "URL:" || label.textContent.trim() === "Search:";
		});
		var urlLabel = directComponentWrapper(labels[0]);
		var urlInput = directComponentWrapper(inputs[0]);
		var openButton = directComponentWrapper(buttons[0]);
		var downloadButton = directComponentWrapper(buttons[1]);
		var inspectButton = directComponentWrapper(buttons[2]);
		var searchLabel = directComponentWrapper(labels[1]);
		var searchInput = directComponentWrapper(inputs[1]);
		var editorButton = directComponentWrapper(buttons[3]);
		var refreshButton = directComponentWrapper(buttons[4]);
		markRemappedControl(urlLabel, "tracker-library-url-label");
		markRemappedControl(urlInput, "tracker-library-url-input");
		markRemappedControl(openButton, "tracker-library-open");
		markRemappedControl(downloadButton, "tracker-library-download");
		markRemappedControl(inspectButton, "tracker-library-inspect");
		markRemappedControl(directComponentWrapper(labels[1]), "tracker-library-search-label");
		markRemappedControl(searchInput, "tracker-library-search-input");
		markRemappedControl(editorButton, "tracker-library-editor");
		markRemappedControl(refreshButton, "tracker-library-refresh");
		bindLibraryUrlInput(inputs[0], buttons[0]);

		var nativeToolbar = toolbarShell.querySelector(".tracker-library-native-toolbar");
		if (!nativeToolbar) {
			nativeToolbar = document.createElement("div");
			nativeToolbar.className = "tracker-library-native-toolbar";
			nativeToolbar.setAttribute("aria-label", "Library browser controls");
			nativeToolbar.innerHTML =
				'<label class="tracker-library-native-field tracker-library-native-url">' +
					'<span>Collection URL</span><input type="url" inputmode="url" autocomplete="off"></label>' +
				'<button type="button" data-library-action="open">Open</button>' +
				'<label class="tracker-library-native-field tracker-library-native-search">' +
					'<span>Search</span><input type="search" inputmode="search" autocomplete="off"></label>' +
				'<button type="button" data-library-action="search">Find</button>' +
				'<div class="tracker-library-native-actions">' +
					'<button type="button" data-library-action="download">Download</button>' +
					'<button type="button" data-library-action="editor">Open editor</button>' +
					'<button type="button" data-library-action="refresh">Refresh</button></div>';
			toolbarShell.appendChild(nativeToolbar);
			var nativeUrl = nativeToolbar.querySelector(".tracker-library-native-url input");
			var nativeSearch = nativeToolbar.querySelector(".tracker-library-native-search input");
			nativeUrl.addEventListener("input", function () {
				inputs[0].value = nativeUrl.value;
				if (nativeUrl.value.trim()) buttons[0].disabled = false;
			});
			nativeSearch.addEventListener("input", function () { inputs[1].value = nativeSearch.value; });
			[
				["open", openButton], ["download", downloadButton], ["search", inspectButton],
				["editor", editorButton], ["refresh", refreshButton]
			].forEach(function (entry) {
				var nativeButton = nativeToolbar.querySelector("[data-library-action='" + entry[0] + "']");
				nativeButton.addEventListener("click", function (event) {
					event.preventDefault();
					event.stopPropagation();
					if (!nativeButton.disabled) invokeLibraryJavaAction(entry[1]);
				});
			});
		}
		var nativeUrlInput = nativeToolbar.querySelector(".tracker-library-native-url input");
		var nativeSearchInput = nativeToolbar.querySelector(".tracker-library-native-search input");
		if (document.activeElement !== nativeUrlInput) nativeUrlInput.value = inputs[0].value || "";
		if (document.activeElement !== nativeSearchInput) nativeSearchInput.value = inputs[1].value || "";
		[
			["open", buttons[0]], ["download", buttons[1]], ["search", buttons[2]],
			["editor", buttons[3]], ["refresh", buttons[4]]
		].forEach(function (entry) {
			var nativeButton = nativeToolbar.querySelector("[data-library-action='" + entry[0] + "']");
			nativeButton.disabled = entry[0] === "open" ? !nativeUrlInput.value.trim() :
				(!entry[1] || entry[1].disabled);
		});
	}

	function enhanceLibraryTrees(swingWindow) {
		Array.prototype.forEach.call(
			swingWindow.querySelectorAll("div[id^='Tracker_TreeUI_'][id$='div']"),
			function (tree) {
				if (!isVisible(tree)) return;
				var canvas = tree.firstElementChild;
				if (!canvas) return;
				tree.classList.add("tracker-library-tree");
				canvas.classList.add("tracker-library-tree-canvas");
				var visualRowHeight = 52;
				var labels = Array.prototype.filter.call(
					canvas.querySelectorAll("label[id^='Tracker_LabelUI_']"),
					function (label) { return isVisible(label); }
				);
				labels.forEach(function (label) {
						var source = [
							label.style.left || "0px",
							label.style.top || "0px",
							label.style.width || "1px",
							label.style.height || "16px"
						].join(",");
						if (label.getAttribute("data-tracker-tree-source") !== source) {
							label.setAttribute("data-tracker-tree-source", source);
							label.setAttribute("data-tracker-logical-tree-bounds", [
								inlinePixels(label, "left", 0),
								inlinePixels(label, "top", 0),
								inlinePixels(label, "width", 1),
								inlinePixels(label, "height", 16)
							].join(","));
						}
						var bounds = (label.getAttribute("data-tracker-logical-tree-bounds") || "0,0,1,16")
							.split(",").map(parseFloat);
					}
				);
				labels.sort(function (a, b) {
					var aTop = parseFloat((a.getAttribute("data-tracker-logical-tree-bounds") || "0,0").split(",")[1]) || 0;
					var bTop = parseFloat((b.getAttribute("data-tracker-logical-tree-bounds") || "0,0").split(",")[1]) || 0;
					return aTop - bTop;
				});
				var visualIndex = 0;
				labels.forEach(function (label, labelIndex) {
					var bounds = (label.getAttribute("data-tracker-logical-tree-bounds") || "0,0,1,16")
						.split(",").map(parseFloat);
					var textNode = label.querySelector("[id$='_txt']");
					var text = textNode ? textNode.textContent.replace(/\u00a0/g, " ").trim() : "";
					var isImplementationRoot = /^::\s*indexTRZdl\.php$/i.test(text);
					var isFile = /\.(?:trz|trk|mp4|m4v|mov|avi|webm|zip|xml|html?|php)$/i.test(text);
					var nextLabel = labels[labelIndex + 1];
					var nextBounds = nextLabel ?
						(nextLabel.getAttribute("data-tracker-logical-tree-bounds") || "0,0,1,16").split(",").map(parseFloat) : null;
					var childrenExpanded = !isFile && nextBounds && nextBounds[0] > bounds[0];
					label.classList.add("tracker-library-tree-label");
					label.classList.toggle("tracker-library-tree-root-file", isImplementationRoot);
					label.classList.toggle("tracker-library-tree-folder", !isFile);
					label.classList.toggle("tracker-library-tree-file", isFile);
					label.classList.toggle("tracker-library-tree-expanded", childrenExpanded);
					label.setAttribute("title", text);
					label.setAttribute("aria-label", (!isFile ? "Folder: " : "File: ") + text);
					label.style.setProperty("--tracker-tree-row-left", Math.max(0, bounds[0]) + "px");
					if (isImplementationRoot) return;
					label.style.setProperty("--tracker-tree-row-top", (visualIndex * visualRowHeight) + "px");
					visualIndex += 1;
				});
				canvas.style.setProperty("--tracker-library-tree-content-height",
					Math.max(tree.clientHeight, visualIndex * visualRowHeight) + "px");
			}
		);
	}

	function libraryLabelText(label) {
		var textNode = label && label.querySelector("[id$='_txt']");
		return textNode ? textNode.textContent.replace(/\u00a0/g, " ").trim() : "";
	}

	function libraryLabelPath(label) {
		var tree = label && label.closest(".tracker-library-tree");
		if (!tree) return "";
		var targetBounds = (label.getAttribute("data-tracker-logical-tree-bounds") || "0,0")
			.split(",").map(parseFloat);
		var rows = Array.prototype.map.call(tree.querySelectorAll(".tracker-library-tree-label"), function (row) {
			var bounds = (row.getAttribute("data-tracker-logical-tree-bounds") || "0,0")
				.split(",").map(parseFloat);
			return { row: row, left: bounds[0] || 0, top: bounds[1] || 0 };
		}).filter(function (item) {
			return item.top <= targetBounds[1] && !item.row.classList.contains("tracker-library-tree-root-file");
		}).sort(function (a, b) { return a.top - b.top; });
		var stack = [];
		for (var i = 0; i < rows.length; i += 1) {
			while (stack.length && stack[stack.length - 1].left >= rows[i].left) stack.pop();
			stack.push({ left: rows[i].left, text: libraryLabelText(rows[i].row) });
			if (rows[i].row === label) break;
		}
		return stack.map(function (item) { return item.text; }).filter(Boolean).join(" / ");
	}

	function updateLibrarySelection(label) {
		var swingWindow = label && label.closest(".tracker-mobile-library-window");
		if (!swingWindow) return;
		Array.prototype.forEach.call(
			swingWindow.querySelectorAll(".tracker-library-tree-label.tracker-library-selected"),
			function (row) { row.classList.remove("tracker-library-selected"); }
		);
		label.classList.add("tracker-library-selected");
		var path = libraryLabelPath(label) || libraryLabelText(label) || "No item selected";
		var readout = swingWindow.querySelector(".tracker-library-path-readout");
		if (readout) {
			readout.textContent = path;
			readout.setAttribute("title", path);
		}
	}

	function setLibraryView(swingWindow, view) {
		if (!swingWindow) return;
		var details = view === "details";
		swingWindow.classList.toggle("tracker-library-show-details", details);
		Array.prototype.forEach.call(
			swingWindow.querySelectorAll(".tracker-library-view-switch button"),
			function (button) {
				button.setAttribute("aria-pressed", String(button.getAttribute("data-library-view") === view));
			}
		);
	}

	function ensureLibraryNavigation(swingWindow, panel) {
		if (!panel || panel.querySelector(":scope > .tracker-library-viewbar")) return;
		var viewbar = document.createElement("div");
		viewbar.className = "tracker-library-viewbar";
		viewbar.innerHTML =
			'<div class="tracker-library-view-switch" role="group" aria-label="Library browser view">' +
			'<button type="button" data-library-view="files" aria-pressed="true">Files</button>' +
			'<button type="button" data-library-view="details" aria-pressed="false">Details</button>' +
			'</div>' +
			'<div class="tracker-library-path"><span>Selected</span>' +
			'<output class="tracker-library-path-readout" title="No item selected">No item selected</output></div>';
		panel.appendChild(viewbar);
		Array.prototype.forEach.call(viewbar.querySelectorAll("button[data-library-view]"), function (button) {
			button.addEventListener("click", function () {
				setLibraryView(swingWindow, button.getAttribute("data-library-view"));
			});
		});
	}

	function enhanceLibrarySplitLayout(swingWindow) {
		var tree = swingWindow.querySelector(".tracker-library-tree");
		var treePane = tree && tree.closest("div[id^='Tracker_ScrollPaneUI_'][id$='div']");
		var split = treePane && treePane.parentElement;
		if (!split || !/^Tracker_SplitPaneUI_/.test(split.id)) return;
		var splitShell = split.parentElement;
		var detailsPane = Array.prototype.find.call(split.children, function (child) {
			return child !== treePane && child.querySelector && child.querySelector(".swingjs-doc");
		});
		if (!detailsPane) {
			detailsPane = Array.prototype.find.call(split.children, function (child) {
				return child !== treePane && /^Tracker_PanelUI_/.test(child.id);
			});
		}
		var divider = Array.prototype.find.call(split.children, function (child) {
			return child !== treePane && child !== detailsPane;
		});
		var activePanel = splitShell && splitShell.closest("div[id^='Tracker_PanelUI_'][id$='div']");
		[splitShell, split].forEach(function (element) {
			if (element) element.classList.add("tracker-library-split");
		});
		if (activePanel) activePanel.classList.add("tracker-library-split-host");
		treePane.classList.add("tracker-library-tree-pane");
		if (detailsPane) detailsPane.classList.add("tracker-library-details-pane");
		if (divider) divider.classList.add("tracker-library-divider");
	}

	function dispatchLibraryTreeRow(label) {
		var tree = label && label.closest(".tracker-library-tree");
		var swingWindow = label && label.closest(".tracker-mobile-library-window");
		var target = tree && tree.firstElementChild;
		if (!target) return;
		var bounds = (label.getAttribute("data-tracker-logical-tree-bounds") || "0,0,1,16")
			.split(",").map(parseFloat);
		var frame = legacyFrame();
		var browser = frame && (frame.libraryBrowser ||
			(typeof frame.getLibraryBrowser$ === "function" ? frame.getLibraryBrowser$() : null));
		var tabbedPane = browser && browser.tabbedPane;
		var panel = tabbedPane && typeof tabbedPane.getSelectedComponent$ === "function" ?
			tabbedPane.getSelectedComponent$() : null;
		var javaTree = panel && panel.tree;
		var rowHeight = Math.max(1, bounds[3] || 16);
		var logicalRow = Math.max(0, Math.round(bounds[1] / rowHeight));
		if (javaTree && typeof javaTree.setSelectionRow$I === "function") {
			/* SwingJS may rebuild or expand the tree after a touch. Keep the selected
			 * Java node as the source of truth and reapply it after those asynchronous
			 * model updates, otherwise the details pane can advance to a nearby row. */
			var isFolder = label.classList.contains("tracker-library-tree-folder");
			javaTree.setSelectionRow$I(logicalRow);
			var selectionPath = typeof javaTree.getPathForRow$I === "function" ?
				javaTree.getPathForRow$I(logicalRow) : null;
			var selectedNode = selectionPath && typeof selectionPath.getLastPathComponent$ === "function" ?
				selectionPath.getLastPathComponent$() : null;
			var selectionToken = String(Date.now()) + ":" + Math.random();
			if (swingWindow) swingWindow._trackerLibrarySelectionToken = selectionToken;
			function applySelectedNode() {
				if (swingWindow && swingWindow._trackerLibrarySelectionToken !== selectionToken) return;
				var currentRow = logicalRow;
				if (selectedNode && typeof javaTree.getRowCount$ === "function" &&
					typeof javaTree.getPathForRow$I === "function") {
					for (var row = 0; row < javaTree.getRowCount$(); row += 1) {
						var path = javaTree.getPathForRow$I(row);
						if (path && typeof path.getLastPathComponent$ === "function" &&
							path.getLastPathComponent$() === selectedNode) {
							currentRow = row;
							break;
						}
					}
				}
				javaTree.setSelectionRow$I(currentRow);
				if (selectedNode && typeof panel.setSelectedNode$org_opensourcephysics_tools_LibraryTreeNode === "function") {
					panel.setSelectedNode$org_opensourcephysics_tools_LibraryTreeNode(selectedNode);
				}
				if (typeof javaTree.scrollRowToVisible$I === "function") javaTree.scrollRowToVisible$I(currentRow);
			}
			global.setTimeout(function () {
				if (isFolder) {
					var expanded = typeof javaTree.isExpanded$I === "function" && javaTree.isExpanded$I(logicalRow);
					if (expanded && typeof javaTree.collapseRow$I === "function") javaTree.collapseRow$I(logicalRow);
					else if (!expanded && typeof javaTree.expandRow$I === "function") javaTree.expandRow$I(logicalRow);
				}
				applySelectedNode();
			}, 0);
			global.setTimeout(applySelectedNode, 100);
			global.setTimeout(applySelectedNode, 300);
			return;
		}
		var treeBounds = tree.getBoundingClientRect();
		/* Dispatch through the saved 16px Swing row, not the enlarged visual row.
		 * SwingJS still resolves pointer coordinates against the pre-enhancement
		 * content origin, so compensate for the native path/view controls added
		 * above the tree. */
		var clientX = treeBounds.left + bounds[0] + Math.min(24, Math.max(4, bounds[2] / 2));
		var viewOffset = swingWindow && swingWindow.classList.contains("tracker-library-compact") ? 148 : 72;
		var clientY = treeBounds.top + bounds[1] + Math.max(1, bounds[3] / 2) - viewOffset;
		["mousedown", "mouseup", "click"].forEach(function (type) {
			var synthetic = new MouseEvent(type, {
				bubbles: true,
				cancelable: true,
				view: global,
				clientX: clientX,
				clientY: clientY,
				button: 0
			});
			Object.defineProperty(synthetic, "trackerLogicalCoordinates", { value: true });
			target.dispatchEvent(synthetic);
		});
	}

	function bindLibraryTreeBridge() {
		if (libraryTreeBridgeBound) return;
		libraryTreeBridgeBound = true;
		var lastLabel = null;
		var lastActivation = 0;
		function handle(event) {
			if (event.trackerLogicalCoordinates) return;
			var label = event.target && typeof event.target.closest === "function" ?
				event.target.closest(".tracker-library-tree-label") : null;
			if (!label) return;
			if (event.type === "pointerdown" && event.pointerType && event.pointerType !== "touch") return;
			event.preventDefault();
			event.stopImmediatePropagation();
			var now = Date.now();
			if (label === lastLabel && now - lastActivation < 650) return;
			lastLabel = label;
			lastActivation = now;
			updateLibrarySelection(label);
			dispatchLibraryTreeRow(label);
		}
		document.addEventListener("pointerdown", handle, true);
		document.addEventListener("touchstart", handle, { capture: true, passive: false });
		document.addEventListener("click", handle, true);
	}

	function enhanceLibraryWindow(swingWindow) {
		swingWindow.classList.add("tracker-mobile-library-window");
		enhanceLibraryToolbar(swingWindow);
		enhanceLibraryTrees(swingWindow);
		enhanceLibrarySplitLayout(swingWindow);
		var root = Array.prototype.find.call(swingWindow.children, function (element) {
			return /Tracker_RootPaneUI_/.test(element.id) && /div$/.test(element.id);
		});
		var layered = root && root.querySelector("div[id^='Tracker_LayeredPaneUI_'][id$='div']");
		var menuBar = layered && layered.querySelector("div[id^='Tracker_MenuBarUI_'][id$='div']");
		var panel = layered && layered.querySelector("div[id^='Tracker_PanelUI_'][id$='div']");
		var toolbar = panel && panel.querySelector("div[id^='Tracker_ToolBarUI_'][id$='div']");
		var content = panel && panel.querySelector("div[id^='Tracker_TabbedPaneUI_'][id$='div']");
		var status = panel && Array.prototype.find.call(panel.querySelectorAll("div[id^='Tracker_ButtonUI_'][id$='div']"), function (element) {
			return inlinePixels(element, "width", 0) > 500;
		});
		if (root) root.classList.add("tracker-library-root");
		if (layered) layered.classList.add("tracker-library-layered");
		if (menuBar) {
			menuBar.classList.add("tracker-library-menubar");
			Array.prototype.forEach.call(
				menuBar.querySelectorAll("div[id^='Tracker_MenuUI_'][id$='div']"),
				function (menu) { markRemappedControl(menu, "tracker-library-menu-control"); }
			);
		}
		if (panel) panel.classList.add("tracker-library-panel");
		if (toolbar) toolbar.classList.add("tracker-library-toolbar-shell");
		if (content) content.classList.add("tracker-library-content");
		if (status) status.classList.add("tracker-library-status");
		ensureLibraryNavigation(swingWindow, panel);
		var editor = swingWindow.querySelector(".swingjs-doc");
		if (editor) editor.classList.add("tracker-library-document");
	}

	function enhanceWindowCloseControl(swingWindow) {
		var closer = swingWindow.querySelector(":scope > [id$='_titlebar'] [id$='_closer']");
		if (!closer) return;
		var savedWindow = (swingWindow.getAttribute("data-tracker-logical-bounds") || "0,0,320,240")
			.split(",");
		var logicalWindowWidth = parseFloat(savedWindow[2]) || 320;
		var logicalWidth = inlinePixels(closer, "width", 20);
		var logicalHeight = inlinePixels(closer, "height", 20);
		closer.setAttribute("data-tracker-logical-bounds", [
			Math.max(0, logicalWindowWidth - logicalWidth), 0, logicalWidth, logicalHeight
		].join(","));
		closer.classList.add("tracker-mobile-remapped-control", "tracker-window-close-control");
	}

	function layoutSwingWindows() {
		windowLayoutQueued = false;
		var viewport = visualViewportRect();
		var compact = compactTouchUI(viewport.width);
		var margin = viewport.width <= 500 ? 4 : 8;
		var availableWidth = Math.max(280, viewport.width - margin * 2);
		var availableHeight = Math.max(220, viewport.height - margin * 2);

			document.querySelectorAll(".swingjs-window").forEach(function (swingWindow) {
			var title = swingWindowTitle(swingWindow);
			if (!title || title === "Tracker Online") return;
			rememberLogicalBounds(swingWindow);
			swingWindow.classList.add("tracker-mobile-responsive-window");
			enhanceWindowCloseControl(swingWindow);
			if (title === "Library Browser") {
				enhanceLibraryWindow(swingWindow);
				swingWindow.classList.toggle("tracker-library-stacked-toolbar", viewport.width <= 640);
				swingWindow.classList.toggle("tracker-library-compact", compactTouchUI(viewport.width));
			}
			if (!compact) {
				swingWindow.classList.remove("tracker-mobile-window-active");
				return;
			}

			var bounds = (swingWindow.getAttribute("data-tracker-logical-bounds") || "0,0,640,480")
				.split(",").map(parseFloat);
			var preferredWidth = title === "Library Browser" ? availableWidth :
				Math.min(availableWidth, Math.max(280, bounds[2] || availableWidth));
			var preferredHeight = title === "Library Browser" ? availableHeight :
				Math.min(availableHeight, Math.max(220, bounds[3] || availableHeight));
			var left = viewport.left + Math.max(margin, Math.floor((viewport.width - preferredWidth) / 2));
			var top = viewport.top + Math.max(margin, Math.floor((viewport.height - preferredHeight) / 2));
			swingWindow.style.setProperty("--tracker-window-left", left + "px");
			swingWindow.style.setProperty("--tracker-window-top", top + "px");
			swingWindow.style.setProperty("--tracker-window-width", preferredWidth + "px");
			swingWindow.style.setProperty("--tracker-window-height", preferredHeight + "px");
			swingWindow.classList.add("tracker-mobile-window-active");
		});
	}

	function queueWindowLayout() {
		if (windowLayoutQueued) return;
		windowLayoutQueued = true;
		global.requestAnimationFrame(layoutSwingWindows);
	}

	function isVisible(element) {
		if (!element) return false;
		var rect = element.getBoundingClientRect();
		var style = global.getComputedStyle(element);
		return rect.width > 0 && rect.height > 0 &&
			style.display !== "none" && style.visibility !== "hidden";
	}

	function libraryRecordName(label) {
		var text = label && label.querySelector("[id$='_txt']");
		return (text ? text.textContent : "").replace(/\u00a0/g, " ").trim();
	}

	function urlFileName(value) {
		var clean = (value || "").split(/[?#]/, 1)[0];
		try {
			clean = decodeURIComponent(clean);
		} catch (error) {
			// Keep the original value if a collection supplies malformed escaping.
		}
		return clean.substring(clean.lastIndexOf("/") + 1);
	}

	function openSelectedLibraryRecord(recordName) {
		var urlInput = Array.prototype.find.call(document.querySelectorAll("input"), function (input) {
			return isVisible(input) && urlFileName(input.value) === recordName;
		});
		/* SwingJS replaces tree labels when selection changes, so reacquire the
		 * stable Library Browser window from its live URL field. */
		var scope = urlInput && urlInput.closest(".swingjs-window");
		if (!scope) return false;
		var openButton = Array.prototype.find.call(scope.querySelectorAll("button"), function (button) {
			return isVisible(button) && !button.disabled &&
				button.textContent.replace(/\u00a0/g, " ").trim() === "Open";
		});
		if (!openButton) return false;
		openButton.click();
		return true;
	}

	function handleLibraryRecordDoubleClick(event) {
		var target = event.target;
		var label = target && typeof target.closest === "function" ?
			target.closest("label[id^='Tracker_LabelUI_']") : null;
		if (!label || !label.closest("div[id^='Tracker_TreeUI_']")) return;
		var recordName = libraryRecordName(label);
		if (!/\.trz$/i.test(recordName)) return;

		/* SwingJS's native double-click tries to reopen the cached /TEMP path.
		 * Suppress it and reuse the Library Browser's proven download/open action. */
		event.preventDefault();
		event.stopImmediatePropagation();
		global.setTimeout(function () {
			openSelectedLibraryRecord(recordName);
		}, 0);
	}

	function bindLibraryBrowserDoubleClick() {
		if (libraryDoubleClickBound) return;
		libraryDoubleClickBound = true;
		document.addEventListener("dblclick", handleLibraryRecordDoubleClick, true);
	}

	function watchPopupMenus() {
		if (popupWatching) return;
		popupWatching = true;
		bindLibraryTreeBridge();
		if (typeof global.MutationObserver === "function") {
			popupObserver = new global.MutationObserver(function () {
				queuePopupClamp();
				queueWindowLayout();
			});
			popupObserver.observe(document.body, {
				attributes: true,
				childList: true,
				subtree: true,
				attributeFilter: ["class", "style"]
			});
		}
		document.addEventListener("click", function () {
			queuePopupClamp();
			global.setTimeout(queuePopupClamp, 80);
		}, true);
		global.addEventListener("pointerdown", handleSubmenuPointer, true);
		global.addEventListener("pointerup", handleSubmenuPointer, true);
		global.addEventListener("touchstart", handleSubmenuPointer, { capture: true, passive: false });
		global.addEventListener("touchend", handleSubmenuPointer, { capture: true, passive: false });
		global.addEventListener("click", handleSubmenuClick, true);
		/* SwingJS may replace DOM observer hooks while the Java applet starts.
		 * A lightweight poll ensures newly materialized submenu rows are bound. */
		if (!popupPoller) {
			popupPoller = global.setInterval(function () {
				enhanceMainToolbar();
				markTouchInteractionSurfaces();
				suppressNativeSwingMenus();
				bindSubmenuTriggers();
				queueWindowLayout();
				if (document.querySelector(".swingjsPopupMenu")) queuePopupClamp();
			}, 120);
		}
	}

	function markTouchInteractionSurfaces() {
		Array.prototype.forEach.call(
			document.querySelectorAll("div[id^='Tracker_PanelUI_']:not([id$='div'])"),
			function (panel) {
				var rect = panel.getBoundingClientRect();
				var scientificSurface = rect.width >= 80 && rect.height >= 60 &&
					!panel.closest(".tracker-mobile-responsive-window") &&
					!panel.closest(".tracker-mobile-submenu-sheet");
				panel.classList.toggle("tracker-touch-interaction-surface", scientificSurface);
			}
		);
	}

	function touchBridgeSurface(target) {
		if (!target || typeof target.closest !== "function") return null;
		if (target.closest("button, input, select, textarea, a, .swingjsPopupMenu, " +
			".tracker-mobile-commandbar, .tracker-mobile-submenu-sheet, " +
			"div[id^='Tracker_MenuBarUI_'], div[id^='Tracker_ToolBarUI_']")) return null;
		return target.closest(".tracker-touch-interaction-surface");
	}

	function currentTrackNeedsMarkModifier() {
		var panel = selectedTrackerPanel();
		var track = panel && typeof panel.getSelectedTrack$ === "function" ? panel.getSelectedTrack$() : null;
		var frame = panel && typeof panel.getFrameNumber$ === "function" ? panel.getFrameNumber$() : 0;
		if (!track || typeof track.getStep$I !== "function") return false;
		return !track.getStep$I(frame);
	}

	function dispatchTouchPointer(source, shiftKey) {
		var target = source.target;
		if (source.type === "pointerdown") {
			var hover = new PointerEvent("pointermove", {
				bubbles: true,
				cancelable: true,
				view: global,
				clientX: source.clientX,
				clientY: source.clientY,
				screenX: source.screenX,
				screenY: source.screenY,
				button: 0,
				buttons: 0,
				pointerId: source.pointerId,
				pointerType: "touch",
				isPrimary: source.isPrimary,
				pressure: 0,
				shiftKey: shiftKey
			});
			Object.defineProperty(hover, "trackerShiftTouchEvent", { value: true });
			target.dispatchEvent(hover);
		}
		var shifted = new PointerEvent(source.type, {
			bubbles: true,
			cancelable: true,
			view: global,
			clientX: source.clientX,
			clientY: source.clientY,
			screenX: source.screenX,
			screenY: source.screenY,
			button: source.button,
			buttons: source.buttons,
			pointerId: source.pointerId,
			pointerType: "touch",
			isPrimary: source.isPrimary,
			pressure: source.pressure,
			shiftKey: shiftKey
		});
		Object.defineProperty(shifted, "trackerShiftTouchEvent", { value: true });
		target.dispatchEvent(shifted);
	}

	function bindTouchMouseBridge() {
		if (touchMouseBridgeBound) return;
		touchMouseBridgeBound = true;
		["pointerdown", "pointermove", "pointerup", "pointercancel"].forEach(function (type) {
			document.addEventListener(type, function (event) {
				if (event.trackerShiftTouchEvent || event.pointerType !== "touch") return;
				if (type === "pointerdown") {
					if (!touchBridgeSurface(event.target)) return;
					activeTouchPointer = {
						pointerId: event.pointerId,
						shiftKey: currentTrackNeedsMarkModifier()
					};
				} else if (!activeTouchPointer || event.pointerId !== activeTouchPointer.pointerId) {
					return;
				}
				event.preventDefault();
				event.stopImmediatePropagation();
				dispatchTouchPointer(event, activeTouchPointer.shiftKey);
				if (type === "pointerup" || type === "pointercancel") activeTouchPointer = null;
			}, true);
		});
	}

	function viewportSize() {
		var viewport = global.visualViewport;
		return {
			width: Math.max(320, Math.floor(viewport ? viewport.width : global.innerWidth)),
			height: Math.max(320, Math.floor(viewport ? viewport.height : global.innerHeight))
		};
	}

	function resizeTracker() {
		resizeQueued = false;
		var frame = legacyFrame();
		if (!frame) return;
		var stage = byId("tracker-stage");
		if (!stage) return;
		var rect = stage.getBoundingClientRect();
		var size = viewportSize();
		var width = Math.max(300, Math.min(size.width, Math.floor(rect.width)) - 12);
		var height = Math.max(260, Math.min(size.height, Math.floor(rect.height)) - 8);
		if (frame && typeof frame.setBounds$I$I$I$I === "function") {
			frame.setBounds$I$I$I$I(4, Math.floor(rect.top) + 4, width, height);
		}
	}

	function queueResize() {
		if (resizeQueued) return;
		resizeQueued = true;
		global.requestAnimationFrame(resizeTracker);
		queuePopupClamp();
		queueWindowLayout();
	}

	function bindResize() {
		global.addEventListener("resize", queueResize, { passive: true });
		global.addEventListener("orientationchange", queueResize, { passive: true });
		if (global.visualViewport) {
			global.visualViewport.addEventListener("resize", queueResize, { passive: true });
			global.visualViewport.addEventListener("scroll", queueResize, { passive: true });
		}
		document.addEventListener("focusin", function (event) {
			if (!event.target || !event.target.closest(".tracker-mobile-responsive-window")) return;
			global.setTimeout(function () {
				queueWindowLayout();
				if (typeof event.target.scrollIntoView === "function") {
					event.target.scrollIntoView({ block: "nearest", inline: "nearest" });
				}
			}, 120);
		}, true);
	}

	function init(trackerApp) {
		app = trackerApp;
		var loading = byId("loading-message");
		if (loading) loading.hidden = true;
		bindResize();
		bindComboTouchBridge();
		bindTouchMouseBridge();
		watchPopupMenus();
		bindLibraryBrowserDoubleClick();
		enhanceMainToolbar();
		markTouchInteractionSurfaces();
		queueResize();
	}

	global.TrackerStudentMobile = {
		init: init,
		showFatalError: showFatalError
	};
})(window);
