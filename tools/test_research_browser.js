#!/usr/bin/env node
'use strict';
// Exercises the shipped inline JavaScript with a small DOM fixture. This verifies
// catalogue behavior; visual layout and media loading require a real browser.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const html = fs.readFileSync(path.join(__dirname,'../docs/research.html'),'utf8');
const scripts = [...html.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/g)];
const payload = scripts[0][1];
const code = scripts[1][1];
const catalogue = JSON.parse(payload);
let checks = 0;
function check(label, fn) {fn();checks++;process.stdout.write('PASS ' + label + '\n');}
function fixture(search = '', storageFails = false) {
  const nodes = new Map(), chips = [], storage = new Map();
  const evidence = {active:null,blob:null,download:null,url:''};
  class Node {
    constructor(tag, text = '') {
      this.tagName = tag;
      this.children = [];
      this._text = text;
      this.value = '';
      this.checked = false;
      this.hidden = false;
      this.dataset = {};
      this.attrs = {};
      this.listeners = {};
    }
    set textContent(value) {this._text = String(value);this.children = [];}
    get textContent() {return this._text + this.children.map(n => n.textContent).join('');}
    get options() {return this.children.filter(n => n.tagName === 'option');}
    append(...items) {
      for (const item of items) {
        if (item.tagName === '#fragment') {this.append(...item.children);continue;}
        item.parent = this;
        this.children.push(item);
      }
    }
    prepend(item) {item.parent = this;this.children.unshift(item);}
    replaceChildren(...items) {this._text = '';this.children = [];this.append(...items);}
    setAttribute(name,value) {this.attrs[name] = String(value);}
    getAttribute(name) {return this.attrs[name] ?? null;}
    addEventListener(type,fn) {(this.listeners[type] ||= []).push(fn);}
    dispatch(type) {for (const fn of this.listeners[type] || []) fn({target:this});}
    click() {if(this.download)evidence.download = this.download;this.dispatch('click');}
    focus() {evidence.active = this;}
    remove() {if(this.parent)this.parent.children = this.parent.children.filter(n => n !== this);}
    querySelector(selector) {
      const matches = n => selector[0] === '.' ? (n.className || '').split(' ').includes(selector.slice(1)) : n.tagName === selector;
      for(const child of this.children) {if(matches(child))return child;const found = child.querySelector(selector);if(found)return found;}
      return null;
    }
  }
  for (const match of html.matchAll(/\bid="([^"]+)"/g)) nodes.set(match[1],new Node('div'));
  nodes.get('catalogue-data').textContent = payload;
  for (const match of html.matchAll(/<select\s+id="([^"]+)">([\s\S]*?)<\/select>/g)) {
    const node = nodes.get(match[1]);node.tagName = 'select';
    for(const option of match[2].matchAll(/<option\s+value="([^"]*)"[^>]*>([\s\S]*?)<\/option>/g)) {
      const item = new Node('option',option[2]);item.value = option[1];node.append(item);
    }
    node.value = node.options[0]?.value || '';
  }
  for(const match of html.matchAll(/<button\s+class="chip"\s+data-query="([^"]*)"[^>]*>([^<]+)<\/button>/g)) {
    const chip = new Node('button',match[2]);chip.className = 'chip';chip.dataset.query = match[1];chips.push(chip);
  }
  const document = {
    getElementById:id => nodes.get(id),
    createElement:tag => new Node(tag),
    createTextNode:text => new Node('#text',text),
    createDocumentFragment:() => new Node('#fragment'),
    querySelectorAll:selector => selector === '.chip' ? chips :
      selector === '.listening-path' ? nodes.get('listeningPaths').children : []
  };
  class TestURL extends URL {
    static createObjectURL(blob) {evidence.blob = blob;return 'blob:test';}
    static revokeObjectURL() {}
  }
  const context = vm.createContext({
    document, URL:TestURL, URLSearchParams, Blob, console,
    location:{pathname:'/JetSetCraft/research.html',search,hash:''},
    history:{replaceState:(_state,_title,url) => {evidence.url = url;}},
    localStorage:{
      getItem:key => {if(storageFails)throw Error('storage disabled');return storage.get(key) ?? null;},
      setItem:(key,value) => {if(storageFails)throw Error('storage disabled');storage.set(key,value);}
    },
    setTimeout:fn => fn()
  });
  vm.runInContext(code,context);
  const run = source => vm.runInContext(source,context);
  const ids = () => JSON.parse(run('JSON.stringify(filtered.map(row => row.id))'));
  const change = (id,value) => {nodes.get(id).value = value;nodes.get(id).dispatch(id === 'search' ? 'input' : 'change');};
  const toggle = (id,value) => {nodes.get(id).checked = value;nodes.get(id).dispatch('change');};
  const reset = () => nodes.get('reset').click();
  return {nodes,chips,storage,evidence,run,ids,change,toggle,reset};
}
async function main() {
  const f = fixture();
  check('default scope includes every curated record',() => assert.equal(f.ids().length,catalogue.new_entries));
  check('initial page is bounded and more control is available',() => {
    assert.equal(f.nodes.get('results').children.length,24);
    assert.equal(f.nodes.get('more').hidden,false);
  });
  f.nodes.get('more').click();
  check('pagination adds cards and focuses the first newly revealed heading',() => {
    assert.equal(f.nodes.get('results').children.length,48);
    assert.equal(f.evidence.active,f.nodes.get('results').children[24].querySelector('h3'));
  });
  f.nodes.get('latest').click();
  check('latest action selects the declared pass and focuses search',() => {
    assert.equal(f.nodes.get('batch').value,catalogue.latest_batch);
    assert.equal(f.ids().length,catalogue.passes.find(p => p.id === catalogue.latest_batch).entry_count);
    assert.equal(f.evidence.active,f.nodes.get('search'));
  });
  f.change('batch','2026-10-31');
  check('first-pass records remain independently retrievable',() => assert.equal(f.ids().length,196));
  f.reset();f.change('category','Music');f.change('search','cúmbia punk');
  check('accent-insensitive multi-term search finds the exact music candidate',() => {
    assert(f.run("filtered.some(r => r.artist === 'Son Rompe Pera' && r.title === 'Cumbia is the New Punk')"));
    assert(!f.run("filtered.some(r => r.title === 'JACK DA FUNK')"));
  });
  check('search and category are encoded in a shareable URL',() => {
    const url = new URL(f.evidence.url,'https://example.test');
    assert.equal(url.searchParams.get('q'),'cúmbia punk');
    assert.equal(url.searchParams.get('category'),'Music');
  });
  f.reset();f.toggle('legacy',true);
  check('including the earlier atlas preserves the complete collection',() => assert.equal(f.ids().length,catalogue.entries.length));
  f.change('category','Legacy atlas');
  check('historical category is isolated',() => assert.equal(f.ids().length,173));
  f.toggle('legacy',false);
  check('disabling history clears contradictory historical filters',() => {
    assert.equal(f.nodes.get('category').value,'');
    assert.equal(f.ids().length,catalogue.new_entries);
  });
  for (const [key,value] of [['priority','Legacy'],['category','Legacy atlas']]) {
    f.reset();f.change(key,value);f.change('batch','2026-10-31');
    check('dated pass clears incompatible historical ' + key,() => {
      assert.equal(f.nodes.get(key).value,'');
      assert.equal(f.ids().length,196);
    });
  }
  for(const listeningPath of catalogue.listening_paths) {
    f.change('journey',listeningPath.id);
    check('ordered listening path: ' + listeningPath.id,() => {
      assert.deepEqual(f.ids(),listeningPath.entry_ids);
      assert.equal(f.nodes.get('category').value,'Music');
      assert.equal(f.nodes.get('pathNote').hidden,false);
    });
  }
  f.change('category','Games');
  check('leaving music clears the listening path',() => {
    assert.equal(f.nodes.get('journey').value,'');
    assert.equal(f.ids().length,catalogue.counts.Games);
  });
  f.nodes.get('listeningPaths').children[0].click();
  check('listening cards activate their ordered selection',() => {
    assert.deepEqual(f.ids(),catalogue.listening_paths[0].entry_ids);
    assert.equal(f.nodes.get('listeningPaths').children[0].getAttribute('aria-pressed'),'true');
  });
  f.change('batch',catalogue.latest_batch);
  check('collection filtering clears a previous listening path',() => assert.equal(f.nodes.get('journey').value,''));
  f.reset();f.change('media','video');
  check('video filter includes actual video links without labeling store pages as videos',() => {
    assert(f.run("filtered.some(r => r.title === 'Bomb Rush Cyberfunk')"));
    assert(f.run("filtered.some(r => r.title.includes('Guilty Gear Xrd technical'))"));
    assert(!f.run("filtered.some(r => r.title === 'HYPERFUNK')"));
  });
  check('a video channel or store listing is not misrepresented as a direct video',() => {
    assert.equal(f.run("videoTarget({media_url:'https://www.youtube.com/@artist'})"),'');
    assert.equal(f.run("videoTarget({media_url:'https://vimeo.com/channels/staffpicks'})"),'');
  });
  f.change('media','image');
  check('image filter retains credited research imagery',() => assert(f.run("filtered.some(r => r.title === 'CustomDance')")));
  f.change('media','music');
  check('audition filter contains exactly the music catalogue',() => assert.equal(f.ids().length,catalogue.counts.Music));
  f.reset();
  f.change('journey',catalogue.listening_paths[0].id);
  f.change('priority','Legacy');
  check('historical priority exits an incompatible music-path scope',() => assert.equal(f.ids().length,173));
  f.reset();
  const firstId = f.ids()[0];
  f.nodes.get('results').children[0].querySelector('.save').click();
  f.toggle('savedOnly',true);
  check('saved shortlist is independent of page size',() => assert.deepEqual(f.ids(),[firstId]));
  check('shortlist survives through the storage contract',() => assert(JSON.parse(f.storage.get('jetsetcraft-research-shortlist-v1')).includes(firstId)));
  f.nodes.get('results').children[0].querySelector('.save').click();
  check('removing the last saved item leaves an accessible empty state',() => {
    assert.equal(f.ids().length,0);
    assert.equal(f.nodes.get('empty').hidden,false);
    assert.equal(f.evidence.active,f.nodes.get('savedOnly'));
  });
  f.reset();f.change('category','Music');f.change('search','footwork');
  const exportCount = f.ids().length;
  f.nodes.get('export').click();
  const csv = await f.evidence.blob.text();
  check('CSV exports all matches and provenance columns',() => {
    assert(csv.includes('"batch","shelf"'));
    assert(csv.includes('"video_url"'));
    assert.equal(csv.split('\r\n').length,exportCount + 1);
    assert.equal(f.evidence.download,'jetsetcraft-research-results.csv');
  });
  f.reset();
  f.run("rows[0].title = '=1+1'; rows[0].lesson = 'A \"quoted\" lesson';");
  f.nodes.get('export').click();
  const guarded = await f.evidence.blob.text();
  check('CSV neutralizes formula-shaped cells and escapes quotes',() => {
    assert(guarded.includes("\"'=1+1\""));
    assert(guarded.includes('A ""quoted"" lesson'));
  });
  const restored = fixture('?path=' + catalogue.listening_paths[0].id);
  check('a listening-path URL restores the curated sequence',() => assert.deepEqual(restored.ids(),catalogue.listening_paths[0].entry_ids));
  const older = fixture('?category=Legacy+atlas');
  check('historical deep links continue to work',() => assert.equal(older.ids().length,173));
  const dated = fixture('?batch=2026-10-31&category=Legacy+atlas&priority=Legacy');
  check('a dated URL clears contradictory historical filters',() => {
    assert.equal(dated.ids().length,196);
    assert.equal(dated.nodes.get('category').value,'');
    assert.equal(dated.nodes.get('priority').value,'');
  });
  const invalid = fixture('?batch=unknown&media=unknown&path=unknown');
  check('invalid filter URL values are ignored',() => assert.equal(invalid.ids().length,catalogue.new_entries));
  const privateMode = fixture('',true);
  privateMode.nodes.get('results').children[0].querySelector('.save').click();
  privateMode.toggle('savedOnly',true);
  check('shortlisting remains usable with unavailable browser storage',() => {
    assert.equal(privateMode.ids().length,1);
    assert(privateMode.nodes.get('storageNote').textContent.includes('session'));
  });
  f.chips.find(chip => chip.dataset.query === 'music').click();
  check('quick searches clear stale collection and saved scopes',() => {
    assert.equal(f.nodes.get('batch').value,'');
    assert.equal(f.nodes.get('journey').value,'');
    assert.equal(f.nodes.get('savedOnly').checked,false);
    assert.equal(f.nodes.get('search').value,'music');
  });
  f.reset();
  check('reset restores the default view and search focus',() => {
    assert.equal(f.ids().length,catalogue.new_entries);
    assert.equal(f.evidence.active,f.nodes.get('search'));
  });
  process.stdout.write('\n' + checks + ' research-browser logic checks passed. Visual/media QA remains a real-browser check.\n');
}
main().catch(error => {console.error(error);process.exitCode = 1;});
