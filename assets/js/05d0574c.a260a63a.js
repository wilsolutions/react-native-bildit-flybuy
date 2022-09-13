(self.webpackChunkmy_website=self.webpackChunkmy_website||[]).push([[1256],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return s},kt:function(){return y}});var r=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function c(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function p(e,t){if(null==e)return{};var n,r,i=function(e,t){if(null==e)return{};var n,r,i={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var a=r.createContext({}),u=function(e){var t=r.useContext(a),n=t;return e&&(n="function"==typeof e?e(t):c(c({},t),e)),n},s=function(e){var t=u(e.components);return r.createElement(a.Provider,{value:t},e.children)},l={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},f=r.forwardRef((function(e,t){var n=e.components,i=e.mdxType,o=e.originalType,a=e.parentName,s=p(e,["components","mdxType","originalType","parentName"]),f=u(n),y=i,d=f["".concat(a,".").concat(y)]||f[y]||l[y]||o;return n?r.createElement(d,c(c({ref:t},s),{},{components:n})):r.createElement(d,c({ref:t},s))}));function y(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var o=n.length,c=new Array(o);c[0]=f;var p={};for(var a in t)hasOwnProperty.call(t,a)&&(p[a]=t[a]);p.originalType=e,p.mdxType="string"==typeof e?e:i,c[1]=p;for(var u=2;u<o;u++)c[u]=n[u];return r.createElement.apply(null,c)}return r.createElement.apply(null,n)}f.displayName="MDXCreateElement"},1413:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return c},metadata:function(){return p},toc:function(){return a},default:function(){return s}});var r=n(2122),i=n(9756),o=(n(7294),n(3905)),c={sidebar_position:1},p={unversionedId:"Types/PickupConfig",id:"Types/PickupConfig",isDocsHomePage:!1,title:"PickupConfig Object Type",description:"PickupConfig object is used to define the pickup config options for an order or site.",source:"@site/docs/Types/PickupConfig.md",sourceDirName:"Types",slug:"/Types/PickupConfig",permalink:"/react-native-bildit-flybuy/docs/Types/PickupConfig",editUrl:"https://github.com/bildit-Platform/react-native-bildit-flybuy/edit/main/website/docs/Types/PickupConfig.md",version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"CustomerInfo Object Type",permalink:"/react-native-bildit-flybuy/docs/Types/CustomerInfo"},next:{title:"PickupTypeConfig Object Type",permalink:"/react-native-bildit-flybuy/docs/Types/PickupTypeConfig"}},a=[{value:"Type",id:"type",children:[]}],u={toc:a};function s(e){var t=e.components,n=(0,i.Z)(e,["components"]);return(0,o.kt)("wrapper",(0,r.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("p",null,(0,o.kt)("inlineCode",{parentName:"p"},"PickupConfig")," object is used to define the pickup config options for an order or site."),(0,o.kt)("h2",{id:"type"},"Type"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-ts"},"{\n  accentColor: string;\n  accentTextColor: string;\n  askToAskImageURL?: string;\n  availablePickupTypes: IPickupTypeConfig[];\n  customerNameEditingEnabled: boolean;\n  id: number;\n  pickupTypeSelectionEnabled: boolean;\n  privacyPolicyURL?: string;\n  termsOfServiceURL?: string;\n  type: string;\n};\n")))}s.isMDXComponent=!0}}]);