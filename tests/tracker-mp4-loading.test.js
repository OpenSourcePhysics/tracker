// Run after the SwingJS build: node tests/tracker-mp4-loading.test.js
const fs = require('fs');
const vm = require('vm');
const assert = require('assert');
const source = fs.readFileSync('site/swingjs/j2s/org/opensourcephysics/media/mov/JSMovieVideo.js', 'utf8');
const body = source.match(/Clazz\.newMeth\(C\$, 'finalizeLoading\$',  function \(\) \{([\s\S]*?)\n\}\);/)[1];
const finalize = vm.runInNewContext('(function(){' + body + '})', {
  $I$: () => {}, Integer: {valueOf$I: n => n}, Double: Number
});
function video(times) {
  return {startTimesMS: times, frameCount: 3, frame: 0, fileName: 'capture.mp4',
    videoDialog: {setVisible$Z() {}}, frameTimes: {size$: () => 3},
    setFrameCount$I(n) {this.initializedFrames = n;},
    setStartTimes$() {this.timingReads++; this.startTimesMS = [0, 100, 200];},
    timingReads: 0, firePropertyChange$S$O$O() {}, setFrameNumber$I(n) {this.seek = n;}
  };
}
const encodedTimes = [0, 67, 203];
const recorded = video(encodedTimes);
// Metadata loading has no enumerated frame list. Reading it caused the freeze.
recorded.frameTimes = {size$() {throw Error('empty metadata frame list was read');}};
finalize.call(recorded);
assert.strictEqual(recorded.startTimesMS, encodedTimes);
assert.strictEqual(recorded.timingReads, 0);
assert.strictEqual(recorded.initializedFrames, 3);
assert.strictEqual(recorded.endFrameNumber, 2);
assert.strictEqual(recorded.seek, -99);
const scanned = video(null);
finalize.call(scanned);
assert.strictEqual(scanned.timingReads, 1);
assert.strictEqual(scanned.initializedFrames, 3);
assert.deepStrictEqual(Array.from(scanned.startTimesMS), [0, 100, 200]);
console.log('PASS: MP4 metadata timing is preserved; scanned frames still initialize timing');
const failureBody = source.match(/function \(err\) \{\nSystem\.err\.println\$S\("JSMovieVideo MediaInfo Error: " \+ err\);([\s\S]*?)\n\}\);/)[1];
const fail = vm.runInNewContext('(function(){' + failureBody + '})', {
  Clazz: {new_: () => []}, $I$: () => {}
});
[true, false].forEach(canSeek => {
  const state = {canSeek, control: {}, v: {startTimesMS: [0], frameTimes: ['old']},
    helper: {next$I() {throw Error('must not restart metadata loading');}},
    next$I(n) {this.fallback = n;}
  };
  fail.call({b$: {'org.opensourcephysics.media.mov.JSMovieVideo.State': state}});
  assert.strictEqual(state.fallback, canSeek ? 0 : 40);
  assert.strictEqual(state.v.startTimesMS, null);
  assert.strictEqual(state.v.frameTimes.length, 0);
});
console.log('PASS: metadata failure schedules frame scanning without retrying MediaInfo');
