/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */const s=(n,e)=>(customElements.define(n,e),e),u=(n,e)=>{const{kind:t,elements:m}=e;return{kind:t,elements:m,finisher(o){customElements.define(n,o)}}},c=n=>e=>typeof e=="function"?s(n,e):u(n,e);export{c};
