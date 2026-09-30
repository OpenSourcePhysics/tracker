// Run with node tests/tracker-mp4-import.test.js. Mock browser capture to test handoff and cancellation.
const assert = require('assert');
const fs = require('fs');
const vm = require('vm');
function setup(mp4, supported) {
 const elements = {};
 const el = id => elements[id] || (elements[id] = {style: {}, value: '10', checked: false, classList: {toggle() {}}, getBoundingClientRect() {return {width: 640, height: 480, left: 0, top: 0};}});
 el('tracker-format-mp4').checked = mp4;
 Object.assign(el('tracker-mp4-preview'), {videoWidth: 640, videoHeight: 480});
 let stopped = 0, draw;
 const stream = {getTracks: () => [{stop() {stopped++;}}]};
 class BlobMock {
  constructor(chunks) {this.size = chunks.length;}
  arrayBuffer() {return Promise.resolve(new Uint8Array([1, 2, 3]).buffer);}
 }
 Object.assign(el('tracker-mp4-canvas'), {getContext: () => ({drawImage(...args) {draw = args;}}), captureStream: () => stream, toBlob(cb) {cb(new BlobMock([1]));}});
 class Recorder {
  static isTypeSupported() {return supported;}
  constructor(s, opts) {this.mimeType = opts.mimeType; this.state = 'inactive';}
  start() {this.state = 'recording';}
  stop() {this.state = 'inactive'; Promise.resolve().then(() => {this.ondataavailable({data: new BlobMock([1])}); this.onstop();});}
 }
 const calls = [];
 const errors = [];
 const context = {Error: function JavaError() {this.toString = () => 'java.lang.Error';}, Clazz: {_Error: Error}, console: {error(...args) {errors.push(args);}}, document: {getElementById: el, querySelectorAll: () => []}, navigator: {}, performance: {now: () => 1000}, Blob: BlobMock, J2S: {_toBytes: b => new Uint8Array(b)}, setInterval: () => 1, clearInterval() {}, setTimeout() {}, MediaRecorder: Recorder};
 context.window = context;
 let code = fs.readFileSync('resources/ES6/tracker-mp4-import.js', 'utf8');
 code = code.replace('\tmount();', '\tglobal.test = {state: state, begin: beginCapture, stop: stopAndImport, toggle: toggleCapture, close: closeDialog, setApp: function(app) {trackerApp = app;}};');
 vm.runInNewContext(code, context);
 const api = context.test;
 api.state.stream = stream;
 api.setApp({importMP4Capture(id, bytes) {calls.push(['mp4', bytes.length]);}, importVideoCapture(id, frames, fps) {calls.push(['images', frames.length, fps]);}});
 return {api, calls, errors, el, stopped: () => stopped, draw: () => draw};
}
(async function () {
 let t = setup(true, true);
 t.api.state.cropEnabled = true;
 t.api.state.crop = {x: .25, y: .25, width: .5, height: .5};
 t.api.begin();
 assert.deepStrictEqual(t.draw().slice(1), [160, 120, 320, 240, 0, 0, 320, 240]);
 await t.api.stop();
 assert.deepStrictEqual(t.calls, [['mp4', 3]]);
 assert(t.stopped() >= 2);
 t = setup(false, false);
 t.api.begin(); await Promise.resolve(); await t.api.stop();
 assert.deepStrictEqual(t.calls, [['images', 1, 10]]);
 t = setup(true, false);
 t.api.begin();
 assert.strictEqual(t.api.state.capturing, false);
 assert.strictEqual(t.el('tracker-mp4-status').textContent, 'MP4 recording is unavailable in this browser. Select Images instead.');
 assert.strictEqual(t.errors.length, 1);
 assert(/MP4 recording is unavailable/.test(t.errors[0][1].stack));
 t = setup(true, true);
 t.api.begin(); const pending = t.api.stop(); t.api.close(); await pending;
 assert.deepStrictEqual(t.calls, []);
 t = setup(true, true);
 t.api.begin();
 t.api.state.recorder.onerror({error: new Error('Encoder failed')});
 for (let i = 0; i < 8; i++) await Promise.resolve();
 assert.deepStrictEqual(t.calls, []);
 assert.strictEqual(t.el('tracker-mp4-status').textContent, 'Encoder failed');
 assert.strictEqual(t.api.state.importing, false);
 t = setup(true, true);
 t.api.toggle();
 assert.strictEqual(t.el('tracker-mp4-record').textContent, 'Stop');
 assert.strictEqual(t.el('tracker-mp4-record').disabled, false);
 await t.api.toggle();
 assert.deepStrictEqual(t.calls, [['mp4', 3]]);
 assert.strictEqual(t.el('tracker-mp4-record').textContent, 'Record');
 assert.strictEqual(t.el('tracker-mp4-record').disabled, true);
 t = setup(false, false);
 t.api.state.sourceKind = 'screen';
 t.api.toggle();
 assert.strictEqual(t.el('tracker-mp4-record').textContent, 'Stop');
 assert.strictEqual(t.api.state.countingDown, true);
 t.api.toggle();
 assert.strictEqual(t.api.state.countingDown, false);
 assert.strictEqual(t.el('tracker-mp4-record').textContent, 'Record');
 assert.strictEqual(t.el('tracker-mp4-record').disabled, false);
 assert.deepStrictEqual(t.calls, []);
 console.log('PASS: MP4 import, cropped canvas, image import, unsupported MP4 with SwingJS Error replacement and console stack, cancellation, encoder error');
})().catch(error => {console.error(error); process.exitCode = 1;});
