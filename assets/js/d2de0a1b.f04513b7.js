(self.webpackChunkmy_website=self.webpackChunkmy_website||[]).push([[2901],{3905:function(e,t,r){"use strict";r.d(t,{Zo:function(){return s},kt:function(){return y}});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function a(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function c(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},i=Object.keys(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var p=n.createContext({}),l=function(e){var t=n.useContext(p),r=t;return e&&(r="function"==typeof e?e(t):a(a({},t),e)),r},s=function(e){var t=l(e.components);return n.createElement(p.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},d=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,i=e.originalType,p=e.parentName,s=c(e,["components","mdxType","originalType","parentName"]),d=l(r),y=o,f=d["".concat(p,".").concat(y)]||d[y]||u[y]||i;return r?n.createElement(f,a(a({ref:t},s),{},{components:r})):n.createElement(f,a({ref:t},s))}));function y(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=r.length,a=new Array(i);a[0]=d;var c={};for(var p in t)hasOwnProperty.call(t,p)&&(c[p]=t[p]);c.originalType=e,c.mdxType="string"==typeof e?e:o,a[1]=c;for(var l=2;l<i;l++)a[l]=r[l];return n.createElement.apply(null,a)}return n.createElement.apply(null,r)}d.displayName="MDXCreateElement"},2402:function(e,t,r){"use strict";r.r(t),r.d(t,{frontMatter:function(){return a},metadata:function(){return c},toc:function(){return p},default:function(){return s}});var n=r(2122),o=r(9756),i=(r(7294),r(3905)),a={sidebar_position:7},c={unversionedId:"Types/OrderState",id:"Types/OrderState",isDocsHomePage:!1,title:"OrderState Object Type",description:"OrderState provides the state of the order from the merchant\u2019s perspective. A pickup order can typically have the following states.",source:"@site/docs/Types/OrderState.md",sourceDirName:"Types",slug:"/Types/OrderState",permalink:"/react-native-bildit-flybuy/docs/Types/OrderState",editUrl:"https://github.com/bildit-Platform/react-native-bildit-flybuy/edit/main/website/docs/Types/OrderState.md",version:"current",sidebarPosition:7,frontMatter:{sidebar_position:7},sidebar:"tutorialSidebar",previous:{title:"NotificationInfo Object Type",permalink:"/react-native-bildit-flybuy/docs/Types/NotificationInfo"},next:{title:"CustomerState Object Type",permalink:"/react-native-bildit-flybuy/docs/Types/CustomerState"}},p=[{value:"Type",id:"type",children:[]}],l={toc:p};function s(e){var t=e.components,r=(0,o.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,n.Z)({},l,r,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"OrderState")," provides the state of the order from the merchant\u2019s perspective. A pickup order can typically have the following states."),(0,i.kt)("h2",{id:"type"},"Type"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-ts"},"enum OrderStateType {\n  CREATED = 'created',\n  READY = 'ready',\n  DELAYED = 'delayed',\n  DELIVERY_DISPATCHED = 'delivery_dispatched',\n  DRIVER_ASSIGNED = 'driver_assigned',\n  DELIVERY_FAILED = 'delivery_failed',\n  PICKED_UP = 'picked_up',\n  OUT_FOR_DELIVERY = 'out_for_delivery',\n  UNDELIVERABLE = 'undeliverable',\n  CANCELLED = 'cancelled',\n  COMPLETED = 'completed',\n}\n")))}s.isMDXComponent=!0}}]);