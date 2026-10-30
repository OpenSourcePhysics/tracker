// Run after the SwingJS build: node tests/crosshair-loading.test.js
const fs = require('fs');
const vm = require('vm');
const assert = require('assert');
const path = require('path');
const source = fs.readFileSync(path.join(__dirname, '../site/swingjs/j2s/org/opensourcephysics/cabrillo/tracker/Crosshair.js'), 'utf8');
const body = source.match(/Clazz\.newMeth\(C\$, 'c\$\$org_opensourcephysics_cabrillo_tracker_TrackerPanel',  function \(panel\) \{([\s\S]*?)\n\}, 1\);/)[1];
const constructor = vm.runInNewContext('(function(panel){' + body + '})', {
  C$: {}, P$: {Crosshair$lambda1: {$init$: {}}},
  Clazz: {super_() {}, new_() {return {}; }},
  $I$: () => ({c$$I: {}, c$$F: {}, c$$I$java_awt_event_ActionListener: {}})
});
function create(mat) {
  const listeners = [];
  const panel = {
    getID$: () => ({$c: () => 1}), getTFrame$: () => ({}),
    addPropertyChangeListener$S$java_beans_PropertyChangeListener: name => listeners.push(name),
    getMat$: () => mat, getImageWidth$: () => 640, getImageHeight$: () => 480
  };
  const crosshair = {zoomPowerMax: 2, zoomPowerMin: 0, setZoomFactor$D() {},
    setXY$D$D(x, y) {this.center = [x, y];}};
  constructor.call(crosshair, panel);
  assert.deepStrictEqual(listeners, ['selectedtrack', 'selectedpoint', 'stepnumber']);
  return crosshair;
}
// Loading tracks on a touch device creates the crosshair before initialize()
// inserts the background mat. Construction must complete so loading can resume.
assert.deepStrictEqual(create(null).center, [320, 240]);
assert.deepStrictEqual(create({getBounds$: () => ({getWidth$: () => 800, getHeight$: () => 600})}).center, [400, 300]);
console.log('PASS: crosshair initializes before the mat exists and preserves mat centering');
