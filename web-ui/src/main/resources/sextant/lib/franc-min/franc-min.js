(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.franc = f()}})(function(){var define,module,exports;return (function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
'use strict'

module.exports = collapse

// `collapse(' \t\nbar \nbaz\t') // ' bar baz '`
function collapse(value) {
  return String(value).replace(/\s+/g, ' ')
}

},{}],2:[function(require,module,exports){
module.exports={
  "Latin": {
    "spa": " de|os |de | la|la | y | a |es |ón |ión|rec|ere|der| co|e l|el |en |ien|ent|cho|ech|ció|aci|o a|a p| el|al |a l|as |e d| en|ona|na |da |s d|nte| to|ad |ene|con| su| pr|tod| se|ho | pe|los|per|ers| lo| ti|cia|o d|n d|a t|cio|ida|res| es|tie|ion|rso|te | in|do |to |son|dad| re| li|e s|tad|que|pro|est|oda|men|nci| po|a e| qu|ue | un|ne |s y|lib|n e|su | na|s e|ia |nac|e e|tra|or | pa|ado|a d|nes|se |ra |a c|com|nal|por|er |a s|ual|rta| o |ber|les|one|rá |des|s p|dos|sta|ser|ter|ar |era|ibe|ert|ale| di|a a|nto|l d|del|ica|hos|o e|io |imi|oci|n c|s n|ant|cci|re |e c|y l|ame| as|mie|enc| so|o s|ici|las|par|s t|ndi| cu|ara|dic|bre|una|tos|ntr|l p|s l|e a|pre|cla|o t|a y|omo|man|y a|ial|so |nid|n l|n p| al|mo |e p|s s| ig|igu|gua|uma| fu|nta|y e|soc|o p|no |ali|ten|s a|ade|hum|ran|l t|n t|s c|ria|dis|d d| ca|cas|das|ada|ido|l e|y d|tiv|vid|mbr|a i| hu|fun|und|eli|s i| ma|nda|e i| ha|uni|nad|a u|sar|s o| ac|die|qui|rac|ndo| tr|ind| me|ori|tal|odo|ari|lid|esp|o y|tic|ca |un |esa|cti|cua|ier|ta |lar|ons|ont|iva|ide|med|edi|d y|ele|nos|ist|l m|s h|ecc|sti|tor| le|seg|cie|r e|n a|ito|ios|rse|ie |o i|a o|o l|pen|tri|rim|l y|ami|lig|erá|o c|rot|ote|mat|ond|ern|n s|e h|an |ect|lo |ibr|ple|sus|us ",
    "eng": " th|the| an|he |nd |and|ion| of|of |tio| to|to |on | in|al |ati|igh|rig|ght| ri|or |ent|as |ll |is |ed |in | be|e r|ne |ver|one|s t|all|eve|t t| fr| ha| re|s a|ty |ery|d t| pr| or|e h| ev| co|ht |e a|ng |his|ts |yon|be |ing|ce |ryo| sh|n t|fre|ree|men|her|pro|has|nal|sha|es |nat|y a|for| hi|hal|n a|f t|nt | pe|n o|s o| fo|nce|d i|er |e s|res|ect|ons|ity|l b|ly |e e|ry |an |e o|ter|ers|e i| de|cti|hts|eed|edo|dom| wh|ona|re | no|l a| a |e p| un| as|ny |d f| wi|nit| na|nte| en|d a|any|ere|ith| di|e c|e t|st |y t|ns |ted|sta|per|th |man|ve |om |nti|s e|t o|ch | ar|d o|equ|soc|oci|wit|ess|ote|ial|rea| al| fu| on| so|uni|oth| ma| ac| se|enc| eq|qua|ual|ive|lit|thi|int| st|tat|r t|t a|y o|e w|hum|uma|und|led|cia|l o|e f| is|le |f h| by|by | la|ali|are|y i|con|te | wo|eas| hu|ave|o a|com| me|ic |r a|ge |f a|ms |whe| ch|en |n e|rot|tec|tit|s r| li|o t|ple|s d|rat|ate|t f|o o|wor| fa|hou|dis|t i|nda|nde|eli|anc|rom| su|cie|no |ary|inc|son|age|at |oms|oun|nst|s w|d w|ld |n p|nta|l p|tan|edu|n s|duc|itl|tle|whi|hic|ich|ble|o s|imi|min|law|aw |gni|iti| ot|g t|eme|se |e b|ntr|tra| pu|d n|s i|act|e d|ort| he|r s|cou|unt|pen|ily| ag|ces|rit|it |din|s f|hav|ind| ed|uca|cat|ren|ien|tho|ern|d e|omm",
    "por": "os |de | de| a | e |o d|to |ão | di|ent|da |ito|em | co|eit|as |dir|ire|es |rei| se|ção|ade|a p|e d|s d|dad|men|nte|do |s e| pe| pr|dos| to| da|o e| o |a a|o a|ess|tod|con| qu|que| do|e a|te |al |res|ida|m d| in|er | ou|sso| re| na|a s| po|uma| li|cia| te|pro|açã|e e|ar |a d|a t|ue | su| es|ou |s p|a e|tos|des|com|ra |ia |tem|no | pa|ame|nto|e p|is |est|oda|na |s o|tra|ões|das|pes|soa|o s|s n|o p|ser|s a| à |ais| as| em|o o|e o|ber|oa |o t|ado|a c|sua|ua | no|ter|man|e s| os|s s|e n|çõe|ica|lib|ibe|erd|rda|nci|odo|nal|so |ntr|or |ura|s t|o c|ona| so| ao|hum|ual|sta|ma |ons|a n|era|e t|pre|ara|r a|por| hu|cio|o à|ria|par|ind|e c|ran|gua| um|o i|a l|s c|ndi|m a| en|und|nos|e r|ano|aci|ion|soc|oci|nid|sen|raç| ac|ndo|nsi| ig|igu| fu|fun|m o|nac|per|ali|rec|ime|ont|açõ|int|r p| al|um | me|a i|s h|nta|rio|cçã|ere|pel|l d|a o| ex|pri|uni|ese|ada| ma|ant|ide|nda| fa|am |e f|lid|io |ém |ita|iva|omo|o r|esp|a f|m p|lic|ca |s f|naç|pod|ode|ver|a q|r e|tad|tiv|vid|e l|o q|r d|e i|seu|eli|mo |ecç|s i|ial|ing|ngu|s l| vi|ist|ta |eci|ênc|a m| ca|der|ido|ios| un|dis|cla|qua|se |ati|sti|r o|sid|roc| tr|sem|o n|ao |dam|ens|tur|ico|rot|ote|tec|sse|l e|ena|for| pl| ni|nin|gué|uém|não|ela|tro|ros|ias",
    "ind": "an |ang| da|ng | pe|ak | ke| me| se|ata|dan|kan| di| be|hak|ber|per|ran|nga|yan|eng| ya| ha|asa|men|gan|ara|nya|n p|n d|n k|a d| at|tan|at |ora|ala| ba|san|erh|ap |ya |rha|n b| ma|a s|pen|g b|eba|as |aan| or|ntu|uk |eti|tia|tas|aka|set|ban|n s| un|n y| te|ter|iap|tuk|k m|beb|bas|lam| de|n m|k a|keb|am |i d|ama|unt|ah |dal|end|n h|p o|den|sa |dak|mem|ika|ra |ebe|pun|ri |nda|ela|ma | sa|di |a m|n t|k d|ngg|n a|tau|asi| ti|eri|gar|man|ada|al |um |un |ari|au |lak|a p|ta |a b|ngs|ole| ne|neg|dar|ers|gsa|ida|leh|ert|k h|ana|sam|sia|i m|ia |dap|era|dil|ila|tid|eh |h d|atu|bat|uka|aha|a a|ai |g d|lan|tu |t d|uan| in|ena|har|sem|ser|kat|erl|apa|erb|uat|na |kum|g s|ung|nan|emp|rta|l d|mas|ega|n u| hu|ka |eni|pat|mba|adi| su|aga|ent|nta|huk|uku|rga|ndi|ind|i s|ar |sua|aku|rus|n i|ni |car|si |nny|han| la|in |u d|lah|ik |gga|ua |ian|ann|lai|usi|emb|rik|mer|erk|arg|emu|dun|dip|nas|lua|aru|ema|a u|min|mat|aya|kes|rak|eka|a t|rka|a k|iba|rbu|rma|yat|ini|ina|anu|nus|mua|s p|ut |lin| ta|us |ndu|da |pem|ami|sya|yar|nak|das|k s|kel|ese|mel| pu|ern|a n|aik|uar|t p|g p|ant|ili|dik| an|tin|ing|ipe|tak|iny|ain| um| ja|aks|sar|rse|aup|upu|seo|eor|g m|g t|dir|pel|ura|bai|aba|erd|eca|h p|kep|m m|jam|umu|mum",
    "fre": " de|es |de |ion|nt |et |tio| et|ent| la|la |e d|on |ne |oit|le |e l| le|s d|t d|ati|e p|roi|it | dr|dro| à | co|té |ns |te |e s|men|re | to|tou| l’|con|que|les| qu| so| pe|des|son|ons|s l| un| pr|ue |s e| pa|e c|ts |t l|onn| au|e a|e e|eme| li|ant|ont|out|ute|ers|res|t à| sa| a |ce |per|tre|a d|er |cti| en|ité|lib| re|en |ux |lle|rso| in| ou|un |à l|nne|nat|une|ou |n d|us |par|nte|ur | se| d’|dan|ans|s s|pro|e t|s p|r l|ire|a p|t p|its|és |ond|sa |a l|nce|é d| dé|nal|aux|omm|me |ert| fo| na|iqu|ect|ale| da| ce|t a|s a|mme|ibe|ber|rté|s c|e r|al |t e| po|our|com|san|qui|e n|ous|r d| ne|fon|au |e o|ell|ali|lit| es| ch|iss|tes|éra|air|s n| di|ter|ui | pl|ar |aut|ien|soc|oci|tra|rat|êtr|int|été|pou|du |est|éga|ran|ain|s o|eur|ona|rs |anc|n c|rai|pri|cla|age|nsi|e m|s t| do|bre|sur|ure|ut | êt| ét|à u|ge |ess|ser|ens| ma|cia|l e| su|n p|a c|ein|st |bli| du|ntr|rés|sen|ndi|ir |n t|a s|soi| ég|ine|l’h|nda|rit| ré|t c|s i|il |l’a|e q| te|é e|t s|qu’|ass|ais|cun|peu|ée |tat|ind|t q|u d|n a| ac|tés|idé|l n|ill| as|’en|ign|gal|hom|nta| fa|lig|ins| on|ie |rel|ote|t i|n s|sse| tr|n e|oir|ple|l’e|s é|ive|a r|rec|nna|ssa| mo|s u|uni|t ê|pré|act| vi|era|sid| nu|e f|pay|’ho|cat|leu|ten|rot|tec|s m",
    "ger": "en |er |der| un|nd |und|ein|ung|cht| de|ich|sch|ng | ge|ie |che| di|die|ech|rec|gen|ine|eit| re| da|ch |n d|ver|hen| zu|t d| au|ht | ha|lic|it |ten|rei| be|in | ei| in| ve|nde|auf|ede|den|n s|zu |uf |ter|ne |fre| je|jed|es | se| an|n u|and|sei|run| fr|at |s r|das|hei|hte|e u|ens|r h|nsc|as |nge| al|ere|hat|men|lle|nte|rde|t a|ese|ner| od|ode| we|g d|n g|all|t u|ers| so|d d|n a|nen|te |lei| vo|wer| gr|ben|ige|e a|ion| st|ege|le |cha| me|ren|n j|haf|aft| er|erk|bei|ent|erd| si|kei|tig|eih|ihe|r d|len|on |n i|lun| gl|chu|e s|ist|st |unt|ern|tli|gem|ges|ft |ati|tio|gru|end|ies|mit|eic|sen|r g|e e|ei | wi|n n| na|sta|gun|ite|n z|r s|gle|chl|lie|mei|em |uch|nat|n w|urc|rch|de |hre| sc|sse|ale|ach|r m|des|n e|spr|t w|r e|d f| ni| du|dur|nie| mi|ied|fen|int|dar|e f|e g|geh|e d|f g|t s|ang|ste|hab|abe|h a|n v|alt|tz |hli|sic|her|nun|eme|ruc|taa|aat|he |e m|erf|ans|geg| is|tun|pru|d g|arf|rf |n o|ndl|ehe|e b|h d|d s|dig|arb|wie|r b| ih|r w|nsp|ber|t i|r a|r v|igk|gke|bes|n r|str|gew|rbe|ema|e v|n h| ar|rt |ind|n f|ins|esc|ieß|ken|ger|eru|ffe|ell|han|igu|man|sam|t g|ohn|hul|rst|tra|rli|lte|hut|utz|ls |ebe|von|r o|e i|nne|etz|d a|rn |isc|sel| fa|one|son|et |aus|r i|det|da |raf|iem|e z|lan|sow",
    "jav": "ng |an |ang| ka|ing|kan| sa|ak |lan| la|hak| ha| pa| ma|ngg|ara|sa |abe|ne | in|ant|n k| ng|nin|tan|nga| an|ata|en |ran|man| ba|ban|ane|ong|ra |n u|hi |nth| da|ake|ke |thi|ung|uwo|won|ngs| uw|asa|ben|gsa|sab|ana|aka|beb|nan|a k|nda|g p|adi|at |awa|san|ni |pan| be|dan|eba|g k|e k|ani|bas|g s|dha|aya| pr|gan|mar|di |ya |wa |g u|n s|ta |a s| wa|arb|e h| na|a n|a l|n p|a b|yan| ut|n n|ah |asi| um|g d|as |han|g n| tu|dar|rbe|wen|ggo| di|dak|mat|sar|eni| un|und|iya|a a|k h|kab|ka |be |uma|art|ora|ngk|i h|ala|rta|n b| or|n m|gar|kar|yat|al |g b|na |a m|n i|ega|ina|kak|g a|pra| ta|gge|ger|ena|kat|kal|a p|i k|tum|oni|nya| ne|adh|g m|duw|uwe|dad|kas| pe| si|aga|uta|k k|pa |and|nun|i l|ngu|go |nal| ke|n a|uju|anu|ama|a d|i p|t m|er | li|per|iba|min|sak|apa|war|ha |pad|ggu|gay|ras|taw|ind|eng|a u|we | bi|n l|ali|awi|neg|awe|bak|g t|e p|ndu|bis| ku|ih |ase| me|iji|pri|bad|eh |i t|uwa|ron|ndh|mra|ar | pi|ur |isa|mba|sin|aba|g l|ebu|n t|ika|men|ini|lak|a w|arg|ku |ami|ayo|a i|nas|liy|e s| we|rib|ngl| ik|k b|e d|rga|rap|tin| lu|aku|bed|k a|h k|yom| as| nd|eka|il | te|umu|rak|ut |dil|i w|i s|jin|kon|jud|wae|ae |kap|uha|uto|tow|gka|umr|n d| ti|eda|gon|ona| mi|ate|mum|um |but|r k|wat|si |k p|k l|gaw",
    "vie": "ng |̣c |́c | qu| th|à |nh | ng|̣i | nh| va|và|̀n |uyê| ph| ca|quy|yề|ền|̀i | ch|̀nh| tr| cu|ngư|i n|gươ|ườ|ời|́t | gi| co|ác|̣t |ó |c t|ự |n t|cá|ông| kh|ượ|ợc| tư| đư|đươ|iệ|ìn|́i | ha| đê|i đ|có|gia| mo|mọ|ọi|như|pha|n n|củ|ủa|̉a |̣n | ba|n c|̀u |̃ng|ân |ều| bi|hôn|ất|tự|g t| vi|n đ|đề|t c| la| ti|nhâ| đô|u c|hiê|bả|ên | tô|hân| do|do |́ q|ch |̀ t| na|́n |ới|ay | hi|àn|̣ d| đi|g n|hay|há| mô|ội|hữ|uố|ốc|n v|̣p |́p |quô|thư| ho|nà|ị |́ch|̀ng|ào|̀o |̉n |ôn |i c| hô|c đ|i v|khô|c h|i t|g v| đa|mộ|ột|́ng|tro|ữn|ướ|ia |̣ng|ản|̉ng|h t|hư |ện|ộc|g c|ả | đo|̉ c|là|c c|n h|n b|hà|iế|̣ t| cô| vê|ức|t t|ã |hộ| vơ|iên|g đ|̉i | bâ|̀y |ớc|a c|̉m | sư|áp|ật|viê|vớ|hươ|tha|ực|h v|ron|ong|g b|qua|iá|̀ c|ể |h c|a m|ế |uậ|ảo|̉o |sự|o v|cho|phâ|n l| mi|hạ|côn|o c|̃ h| cư|ục|̀ n| hơ|i h|c q|á |ại|bị|cả|c n| lu|ín|h đ| xa|g h|độ|bấ| nư|m v|thô| tâ|tộ|hả|oà|áo|́o |ốn|ệ |thu|mì| du|̣ c|xã|c p|ải| hư|́ c|ho |y t|o n|n p|ở |hứ|iể|y đ|hấ|ối|chi|án|ề |́ t|ệc|cũ|ũn|tiê|hợ|ợp|o h|hoa|ày|ai |ết|̉ n|c b|đó| đâ|luâ|đố|kha|về|̉ t|c l|̀ đ|i b|nươ| bă|dụ|họ| ta|thê|tri|hí|́nh|g q|p q|n g|o t|c g|hự|yên|i l|́u |an | cơ",
    "ita": " di|to |ion| de| in|la |e d|ne |di | e |zio|re |le |ni |ell|one|lla|a d|rit|o d|itt|del| co|dir|iri|ti |ess|ent| al|azi|tto|te |i d|i i|ere|tà | pr|ndi|e l|ale|ind|o a|e e|gni|e i|nte|con|li |a s| un|i e|ogn|men|uo | og| ne|idu|ivi|e a|div|vid|duo| ha|tti| es|a p|no | li|za |pro|ato|all|sse|per|ser| so|i s| la| su|e p| pe|a l|na |ibe|ali| il|il |e n|lib|ber|e c|ia |ha |che|e s|o s|o e| qu|in |nza|ta |nto| ri|he |o i|oni|sta| o | a |o c|nel|e o|naz|so |o p|o h|gli| po|i u|ond|i p|ame|ers|i c|ver|ro |ri |era|un |lle|a c|ua | ch|ssi|una|el |i a|ert|rtà| l |a a|tat|ant|dis|ei |a e| si| ad|à e|nal| da| le|est|pri|nit|ter|ual| st|ona|are|ità|dei|cia|gua|anz|tut| pa|al | ed| re|sua|ono| na|uni|raz|si |ita|com|ist|man|ed |der|ad |i o|enz|soc|que|res| se|o o|ese| tu|i r|io |ett|à d|on |dic|sia|rso|se |uma|ani|rio|ari|ial|eri|ien|ll |oci|rat|tra|ich|pre|qua|do | um|a t|i l|zza|sci|tri|er |ico|pos|a n|ara|o n|son|att| fo|fon|nda|utt|par|nti|sti|nes|n c| i |chi|hia|iar|int|sen|e u|str|uzi|ati|a r|rop|opr|egu| me|ra |ann| ma| eg|ost|bil|isp|ues| no|ont|rà |tta|ina|ezz|l i|tal| ra|gio|nno|a i|d a|i m|ria| cu|ore|e r|izi|dev|tan|lit|cie|non|sso|sun|ite|ica|l d|ide|lia|cos|i n|nta|a f| is|l p|art",
    "tur": " ve| ha|ve |ler|lar|ir |in |hak| he|her|bir|arı|er |an |eri| bi|ya |r h|ak |ın |eti|iye|ası| ka|yet| ol|tle|ını|ara|eya|akk|kkı|etl|sın|na |esi|de |ek | ta|nda|ini| bu|rın|ile|vey|kla|rin|ne |e h|ır |ine|e k|ına|sin|dır|ere| sa|n h|ama|ınd|nın|mas| ge|le |ı v| va|erk|rke|lma|nma|lan| te|tin|akl|rle|nin|en |e m|ard|a v|ill| de|let|da |k h| me|aya| şa|k v| hü|riy|e b|kın|nı |et |dan|san|e d|var|rdı|kes|si |mil|e a| il|hür|ana|ret|dir| se|şah|mes|irl| mi|ola|bu |ürr|rri|n e|n i|kı |n v|mek| ma|mak|lle|lik|nsa|li |ı h| iş| ed| iç|n b|kar| ba|ala| hi|eli|ulu|a h|eme|re |e s|ni |e t|n k|a b|iş |rak|evl|e i|etm|ik |r ş|ar | eş|olm|un |hai|aiz|izd|zdi|im |dil|n t|nde| gö|ilm|lme|tir|mal|hiç|e g|unm|ma |ele|a i|e e|eşi|şit|ık |mel| et| ko|n s|ahs|i v|sı | an|el |yla|la |ili|r v|rı |anı|ede|ket| ya|lun|may|se |ins|tim|edi|siy|t v|içi|çin|a k|nla|r b|miş|i b|yan|ame|tme| da|bul|mem|eml|eke|mle| ki| ke|lek| in| di|din|uğu|n m|it |ser|ind| mü|arş|rşı|es |ger|a s|len| ay| ku|vle|erd|eye|ye |oru|nam|ken| uy|a m|ün |r a|i i|tür|i m|kor| so|al |hsı|cak|rme|nun|lam|eni|dev|rde|ri |mey|a d|i o|kim|ims|mse|end|ndi|rek|ahi|il |hay|lık|e v|iç |sız| öğ|öğr|ğre| bü|büt|ütü|tün|anl|alı|şma|k g|at |den|i s",
    "pol": " pr|nie| i |ie |pra| po|ani|raw|ia |nia|go |wie| do|ch |ego|iek|owi| ni|ści|ci |awo|a p|do | cz|ośc|ych| ma|ek |rze|prz| na|wo | za| w |ej |noś|czł|zło|eni| je|wa |łow|i p|wol|oln| lu|rod| ka|wsz| wo|lno|y c|ma |każ|ażd|ny |dy |o d|stw|owa|żdy| wy|rzy|ecz|sta| sw|e p|twa|czn|dzi|i w|szy|zys|na |ów |lub|ub |a w|k m|est| sp|kie|wan|ają| ws|pow|e w|spo|nyc|pos|rac|a i|cze|yst|ać |neg|sze|ne |mi |aro|ńst| ja|jak|o p|pod| z |acj|obo| ko|i i|nar|i n| ro|awa| ró|zy |dow|zen|zan|zne|zec|jąc|iej|cy |rów|nej|odn|nic|czy|o s|no |ony|aw |i z|ówn|odz|jeg|o w|edn|o z|aki|o o|a s| st|ni |bez|owo| in|ien|eńs|ami| or|dno|zie|mie| ob|kol|stę|tęp|i k|ez |w c|poł|ołe|łec|ym |orz|jed|o u| os|olw|lwi|wia|ka |owy|owe|y w| be|o n|jes|wob|wyc|a j| od|zna|inn|zyn|aln|któ|cji|ji |się|i s|raz|y s|lud| kr|ją |cza|zes|nik|st |swo|a o|sza|ora|icz|kra|a z|h p|i o|ost|roz|war|ara|że |lni|raj| si|ię |e o|a n|em |eka|stk|tki|pop|ą p|iec|ron|kow|odo|w p|peł|ełn|ran|wni|dni|ows|ech|gan|dów|zon|pie|a d|i l| kt|tór|ini|ejs| de|dek|ywa|iko|z w|god|ków|adz|dst|taw| to|trz|e i|ich|dzy|by |bod|iu |nan|h i|chn|zeń|y z|ano|udz|ieg|w z|ier|ale|a k|z p|zaw|ekl|kla|lar|any|du | zw| go|o r|to |az |y n|ods|ymi|ju |och|nau|wej|i m",
    "gax": "aa |an |uu | ka|ni |aan|umm|ii |mma|maa| wa|ti | fi|nam|ta |tti| na|saa| mi|fi |rga|i k|a n| qa|dha|iyy|oot|mir|irg|in |raa|qab|a i|kan|a k|isa|chu|akk|amu|aba|a f|huu|kam| ta|kka|amn|ami| is|a a|mni|att| bi|yaa|ach|yyu|yuu|ee |miy|wal|waa|ga |aat|ata|a e|tii|oo | ni| ee|moo|ba |ota| ak|a h| ga|i q| dh|daa|a m|haa|ama|i a|a b|yoo|ka |kaa| hi|aas|sum|u n| uu|arg| hu|man| ha| ar| ke| yo| ba|ees|i i|taa|uuf|uf |ada|iin|i f|rra|ani|a w|i w| ad|da |nya|a u|irr|na |hun|isu|hin| ma| ho|ess|und|i m|i b|bar|is |een|ana|mu |bu |i d| sa|f m|add|sa |eeg| ir|i h|n h|u a|aad| la|al |ala|udh|ira|hoj|kee|goo| ya|ook|abu|gac|mum|as |itt|nda|see|n t|n i|uum|n k|ra |rgo|ara|a q|ums|muu|mat|a d|nii|sii|ssa|ati|a g|asu|biy|yya|eef|haw| da| mo|tum|a t|u h|gar|uma|a s|n a|n b|baa|awa|nis|eny|u y|roo|mmo|gam|sat|abs|n m|tee|nna|eer|bir| ku| qo|bil|ili|lis|otu| to|kko|n w|ali|rum|msa|rka| fu|amm|gaa|aaf|era|ya | ye|yer|ero|oti|kun|un |jii|ald|i y|ant|suu|n d|tok|okk|ame|mee|nni|tan| am|lii|n u|aru|lee|gum|ddu|i g|u m|oji|ura|lda|lab|ila|laa|aal|n y|ef |chi|uud| qi|qix|dar|ark|dda|gal|u b| ji|jir|han|art|arb|asa|ega|tam|hii|ffa| se| bu|faa|ndi|n g|bat|oka|kar| mu|mur|aja|uun|naa|sad|a l|lam|ken|enn|u f|egu",
    "swh": "a k|wa |na | ya| ku|ya | na| wa|a m| ha|i y|a h|a n|ana|ki |aki|kwa| kw|hak| ka| ma|la |a w|tu |li |a u|ni |i k|a a|ila| ki|ali|a y|ati|za |ifa|ili| mt|ke | an|kil|kat|mtu|ake|ote|te |ma |ika|ka |we |a s|yo |i n|fa |ata|e k|ama|zi |u a|amb|ia |kut|ina|u w|azi| yo|i z|asi| za|o y|au |yak|uhu|ish|tik|ha |wat| au|u k|e a|mba|hur| bi|ara|sha|uru|mu | as| hi|u n|hi |ru |tai|aif|a b|hal|ayo|cha| uh|i h|yot| ch|awa|chi|atu|e n| zi|u y|ngi|mat|shi|ani|e y|sa |eri|ja |uli| am|ele|i a|end|o k| sh|ima|ami|oja|a t| ta| nc|nch|any|a j|ham|wan|ada|uta|i w|iki|ra |moj|ii |ari|kuw|uwa|ye | la| wo|o h| sa|ti |wak|she|iwe|kan|nay|eza|iri|iwa|fan|bu |i m|uto|lim|ao |her|ria|wen|kam|di | ja|jam| ni|ing|a l|wot|bin|amu|dha|o w|ahi|kaz|zo |da |adh|si | hu|ri |bil|e m|aka|e w|ang|ini|agu|sta|a z|kup|kul|lin|ind|ne |aji|zim|nya|kus|har|nye|asa|nad|dam|rik|iyo| ba|bar| nd|nde|ita|ta |gin|ine|uu |mwe|maa|ndi|kuf|o n|u h|i s|uzi|nga| ye|tah|sil|imu| ut|azo|esh|uni|taa|aar|rif|hii|wez|uba|wam|ush|mbo|bo |ibu|lez|wal|saw|kos|e b|a c| si|aza|tok|oka|tak|eng|dhi|ala|hir|yan|izo|ten|guz| mw|liw|ndo|oa |laz|aha|uku|ian|eli|mam|ua |ndw|zin|aba|pat|del|i b|ufu|nda|a i|mai|ais| um|man|ba |u m|kuh|zwa|sia|tan|taw|e i",
    "sun": "an |na |eun| ka|ng | sa|ana|ang| di| ha|ak |nga|hak|un |ung|keu| ba|anu| an|nu |a b| bo| je|a h|ata|asa|jeu|ina| ng|ara|nan|gan|sa |a k|awa|ah | na|n k|kan|aha|ga |a s|a p|ban| ma|a n|bog|oga|ing|sar| ku| pa|man|a a|ha |san|bae|ae |din|g s|sah|tan|aga|ra | si|ala|kat|n s| pe|ma | ti|per|aya|sin| te| pi| at|n a|aan|pan|lah|gar|n n|u d|ta |eu |kum|ari|ngs|ran|a d|n d|n b|gsa|a m|wa |ama|ku |ike|taw|n p|k h|al | ja|eba|bas|a t|at |ika|beb|asi|atu|pik|kab|una|nda|a j|e b|n h|nag|oh |aba|en |ila|g k|boh|aku|ngg|art|rta|abe|ar |ima|n j|um |di |usa|udu|geu|k a|adi|ola|sak|aca|u s|rim| ay|car|h k|aka|eh |teu|tin| me| po|ti |awe|ngt|sac|jen|u a|uma|ent|k n|gaw|law|dan|uku|ur |teh|h s|bar|aru|ate| hu|nar|n t|jal|aja|dil|ere|iba|ieu|pol|nya|ut |wan|are|mas|ake|upa|pa |yan|huk| so|nus|ngk| du|ura|tun|ya |mpa|isa|lan| ge| mi|u m|kal|uan|ern|tut|tay|h b|hna|kaw|kud|us |und|ena|n m|han|nte|lak| ie|ula|ka | ke|rup| tu|u k| nu|g n|umn|mna|h p|g d|u n|gta|ayu|yun|mba|gam| be|du | ta| wa|wat|eus|a u|ren|umu|i m|ri |eri|rik|u p|dit|ali|h a|k k|k d|ngu|rua|ua | da|amp|men|sal|nge| ra|sas|nas|ona| bi|ame|sab|alm|lma|ami|min|il |kas|ter|mum|rak|mer|ksa|k j|yat|wil|mar|eur|g b|war|gal|kaa|we |tur|e a|r k",
    "ron": " de|și | și|re | în|are|te |de |ea |ul |rep|le |dre|ept|e d| dr|ie |e a|ate|în |tul|ptu| sa| pr|e p|or | pe|la |e s|ori| la| co|lor| or|ii |rea|ce |tat|au | a |ați| ca|ent|ale| fi|ă a| ar|a s|ice|per|ers|uri| li|a d|al |ric| re|e c|e o|nă |i s|ei |tur|men|con| să|lib|ibe|ber|să |rso|tăț|ilo| ac|sau|pri|ăți|i a|i l|l l|car| in|ter|ție|lă |că |tea|a p|ții|soa|oan|ri |nal|in | al|e ș|i î|ril|ană|pre|ui |uni|e f|se |ile|ere|i d|ita| un|ert|e î|a o|ia |i c|fie|ele|ace|i ș|nte|tă |pen|ntr| se|a l|pro| că|ire|ală|eni|est| ni|ă d|lui|a c| cu|n c| nu|ona|sal| as|eri|naț|ând|ră | om| su|ecu|i p|rin|e n|ici|i n|nu |oat|inț|ni |tre| to|tor|ări|soc|oci|ste| na|iun| di| po|l d|va |ega|gal| tr|ă p|ulu|n m|ă î|a a|rec|res|i o| so|fi |sta|sa |uie| au|lit| ce| mo|din|ces|nic|int|nd |i e|cla|ara|ons| îm| eg|a î|rel|e l|ial|i f| fa|ță |leg|e e|tar|ra |ă f|a f|rar|iei|nit|ă c|tru|ru |u d|act|at |rtă|ți |nta|nde|eme|ntu|ame|reb|ebu|bui|toa|l c| o |ion|ă ș|dep|ali|ât |ili|ect|ite|i i|pli|n a|dec|rta|cu |împ|cți|ane|e r|văț|nt |u c|ța |l ș|cia|țio|ită|bil|r ș|poa|ca | st|t î|tri|riv|man|ne |omu|rie|rit|înv|nvă|ăță|mân|mod|od |rot| ma|cur|u a|oar|uma|a ș|rii|era| ex|tra|iil|ani|țiu|lic|t s|nța|eze|ta | va",
    "hau": "da | da|in |a k|ya |an |a d|a a| ya| ko| wa| a |sa |na | ha|a s|ta |kin|wan|wa | ta| ba|a y|a h|n d|n a|iya|ko |a t|ar |ma | na|yan| sa|ba |asa| za| ma|hak|a w| ka|ata|ama|akk|i d|a m| mu|su |owa|a z|iki| ƙa|nci|a b| ci|ai | sh|kow|anc|nsa|a ƙ|a c| su|shi|ka | ku| ga|ne |ani|ci |e d|kum|uma|‘ya|cik| du|uwa|ana| ‘y|i k|ali|ɗan| yi|ada|ƙas|aka|kki|utu|n y|hi |a n| ad| do| ra|mut|tar| ɗa| ab|nda|a g|man|nan|ars|cin|ane|and|n k|min|yi |i a|ke |sam|ins|a i|nin|yin|ki |tum|ni |aɗa|ann|e m|ami|dam|za |en |kan|um |yar|mi |duk|oka|n h| ja|dai|kam|ewa|mat|i y|nna|abi|ash|n s|waɗ|ida|am |re |ga |sar|kok|oki|una|mas|ra |i b|dun|uni|abu|a ‘| ƙu|n j|awa|ce |a r|e s|ara|a ɗ|san|li |aba|cew|she|ƙun|kar|ari|m n|niy| ai|aik|u d|kko|buw|n w| la| ne|rsa|zam|omi|rin|hal|bub|ubu|aya|a l|han|ban|o n|are|add|i m|zai| hu|me |bin|tsa|sas|i s|ake|n ‘| fa|kiy|n m|ans|dom| ce|r d|uns|ƙar| an|jam|ɗin|i w| am|n t|wat|ika|yya|nce|har|ame|gan|hen|n b|n ƙ|dok|fa | ki|yak|ray|abb|din|on |bay|aid|ayi|aci|dan|aur|ili|u k| al|rsu| ts|ukk|kka|aye|nsu|ayu|bba| id|ant|n r|o d|sun|tun|unc|sha| lo|lok|kac|aif|fi |gam|aga|un |lin|aɗi|yuw|aja|fan|i i|ace|uka|n i|war|riy|imi|sak| ir|yay|tab|bat|mar| iy|sab|nta|afi|o a| ak|bi ",
    "fuv": "de | e |e n|nde| ha|la |e e|akk| ka| nd|ina| wa|al |hak|na | in|ndi|ɗo |kke|ii |di |aad|ade|um |ko |i h|ala| mu| ne|lla|ji |wal| jo|mum| fo|all|neɗ|eɗɗ| le| ko|e h|kal|taa|re |aaw| ng|e k|aa |e w|ee |ley|jog|ke |e m|laa|nnd|eed|e l|ɗɗo|aag|ol | ta|o k|kee|gu |ti |dee|a j|ogi|waa|m e|am |le |eji|ond|nga|gal| wo|ɓe |ɗe |e d|awa|gii|ede|eej| re|gol|aan| go|agu|i e|oti|ann|fot|eyd|e t|ɗee|naa|oto|ydi| po|pot|maa| he|een|i n|enn|ni |taw|a i|e j|e f|a k|goo|to |dim|der|ele| aa|o n| de| fa| ba|ngu|oot|er |dir|won|oor| sa|ngo|ka |ndo|i k|a n|ay |ota|a f|ima|e ɓ| to|i f|a e|tee|ren| ja|i w|wa |o f|fof|ore|eyɗ|yɗe|a w|too|ma |o t|awi|i m|kam|o e|hay|and|nan|ñaa|e y|of |eel|e s|hee|aak|nka| do|l e|e g|ira| la| so| ɓe|a t|dii|e i| te|tin|e r|e p|o w|ani|aar|are| na|ral| ña| yi|awo| ya|so |aam|i l| ho|oo |ooj|nng|nge|woo| ma|faw|kaa| mo|u m|und|dow|gaa|en |o i| li|lig|igg|e a|ita|e b| o | nj| mb|o h|nda|ude|ɗi | no|haa|a h| fe| di|iin|iti|tii|yan| tu|tuu|inn|ama|baa|iiɗ|den|tal|aaɗ|yim|imɓ|njo|edd|ine|nee| je|jey|lli|lit|uug|ugn|no |bel|go | hu|ank|je |do |guu| da|mii| ke|a d|ano|non|l n|y g| ɗu|gna|mɓe|ete|i a|wit|jaŋ|aŋd|ŋde| su|alt| ɗe|nna|a a| ɓa|ɓam|amt|tde|ago|l h|m t|ind|ɗɗa|aga|eɗe|ow ",
    "bos": " pr| i |je |rav| na|pra|na |da |ma |ima| sv|a s|nje|a p| da| po|anj|a i|vo |ko |ja |va | u |ako|o i|no | za|ju |e s| im|avo|ti |sva|ava|o n|i p|li |ili|i s|ost|van|vak| ko|ih |ne |a u| sl|nja|jed| ne| dr|koj|ije|i d| bi|stv|im |u s| il|slo|lob|obo|bod| je| ra|pri|sti|vje| ob|a d|om |se | su|e i|a n| se|i i|dru|enj| os|a b|e p|voj|cij|u p|o d|a o|raz|su |i n|uje|ova|u i|edn| nj| od|i u|u o|lo |ran|lju|ni |jen|ovo|aci|iti|o p|a k|oje|žav|nos|dje|e o|bra|pre|a j|pro|ji |i o| ka|nih|bit|jeg| tr|tre|bud|u z|og |sta|drž|rža|e d|u n|pos|mij|elj|svo|reb| bu|avn|jem|ija|e b|ći |aro|rod|red|ba |a z|šti|ka |de |em |aju|iva|lje|ve |e u|jel|jer|bil|ilo| iz|eni|du | do| st|a t|za |tu |nar|tva|odn|gov| sa|nim|m i|e n|vim| ni|u d|o k|oji| sm|dna|ući|ist|i m|eba|ičn|vno| dj|oda|nak|e k|an |nov|sno|stu|aln|nst|eno|eđu|čno|ani|nom|olj|tiv|nac|ave|i b|smi|čov|ovj|osn|a r|nap|ovi|ans|dno|jan|nju|oja|nog|m s|edi|ara|oj |nu |kri| kr|odu|iko|lja|sto|rad|nik|tup| čo|jek|tvo| vj| mi|tel|obr|živ|tit|una|ego|pod|sam|o o|rug| op|nji|din| mo|vu | ov|h p|udu|riv|dst|te | te|a č|vni|svi|i v|ina|i t|ite|o s|u u|m n|zaš|ašt|itu|ak |dni|nic|nič|odr|vol|avi|g p| ta|rim|kla|e t|ao | vr|akv|tno|mje|duć|ona|ada|obi|eds",
    "hrv": " pr| i |je |rav|pra|ma | na|ima| sv|na |ti |a p|nje| po|a s|anj|a i|vo |ko |da |vat|va |no |o i| za|ja |i s|avo| im|sva| u |i p|e s| bi|tko|ju |o n|li |ili|van|ava| sl|ih |ije| dr|ne |ost|jed| ne|u s|ova|nja| os| da| ra| ko|slo|lob|obo|bod|atk|i d|koj|iti| il|stv|pri|im |om | ob| je| su|vje|i u|i n|e i|i i| ka|bit|dru|ati|se |voj|i o|a b|a o|ćen|ući|a n| se|o p|enj|edn|a u|sti| mo|ćav|lo |dje|raz| od|ran|u p|rod|a k|ni |su |mij|u i|svo|ako|a j|aro|drć|rća|pos|eno|e p|pre| nj|e o|ćiv|nar|ji |oje|e n|eni|nih|oda|ći |nov|bra|ra |nim|a d|avn|og |aju|iva|ovo|nos|i b|bil|sno|za |ovi|red|tva|a z|mor|ora|ka |sta|jem|pro|jen|u o|cij|ve |e d|jel|jer|ilo| do|osn|i m|odn| iz|nom|lju|em |lje| ni|aci|oji|o d|du | st|nit|elj|u z|jeg| sa|o o|m i|vno|vim|uje|e b|oj | dj|rad| sm|dna|nak|e k|an |stu|o s|tit|tu |aln|nst|eću|dno|gov|ani|juć|u d|m s|e u|a ć|u u|nju| bu|bud|te |ćov|ovj|tvo|a r|nap|šti|ist|ću |ans|m p|jan|nić|olj|u n|edi|ara|nu |o k|udu|ona|smi|odu|ada|oja|tup| ćo|jek| vj|ina| mi|tel|i v|obr|zaš|ašt|una|dni|ija|pod|sam|duć|rug| op| ta|nji|e m|oso|sob|h p|itk|svi|ite|elo|itu|meć|jim|odr|di |vol|avi|nog|štv|rim|din|kla|će |ao |tno| ći|kak|akv|ave|nac|lja|sto|obi| te|to |vi |ovn|vni|odi|lji",
    "nld": "en |de |an | de|van| va| en| he|ing|cht|der|ng |n d|n v|et |een|ech| ge|n e|ver|rec|nde| ee| re| be|ede|er |e v|gen|het|den| te|ten| op| in|n i| ve|lij| zi|zij|ere|eli|ijk|oor|ht |te |ens|n o|and|t o|ied|ijn| on|ke |op |eid| vo|jn |id |ond|in |sch| vr|n z|rde|aan| ie|aar|ren|men|rij|hei|ord|hte|eft| we|ft |n g|n w|or |n h|eef| me|wor|vri|t r|hee|al |le |of |ati| of|g v|lle|e b| wo|eni| aa|voo|r h|n a| al|nd |e o|n t|ege|erk|t h|jke| na|sta|at | da|e e|end|nat| st|nge|ste|e g|tie|n b|om |die|e r|r d|erw|ij |dig|e s| om|wel|t e|ige|ter|gel|ie |e m|re |t d| za|ers|ijh|jhe|d v|zal|nig|nie|bes|ns |e w|est|d e|g e|e n|ele| do|ge |vol|che|e d|ig |gin|eze|nst|ona|eke|cha|hap|dat|lke|e a| di|waa| to|min|jk |tel| gr|len|eme|lin|elk|ard|doo| wa|eve|ven|n s|str|gro|han|del|ich| ov|ove|n n|t v|tio|ion|wet|it |gem|ijd|met| zo|uit|aat|dez|ze |rin|e i|all|st |ach| ni|toe|n m|ies|es |taa|per|hed|heb|ebb|bbe|ien|sti| ma|nte|ale|kin|nin|mee|daa|el |ben|ema|man|s e|e h|esc|her|lan|ang|ete|g o|wer|is | er|pen|nsc|beg|igd|t g|ont|iet|tig|ron|tin|p v|r e|rwi|wij|ijs| hu|erm|nal|bij|eer|edi|ite|t a|t w|d o|naa|weg|iem|g d|teg|ert|arb|als|d z|tan|tre| la|ar |ame|js |rmi|t b|app|rwe| bi|t z|ker|eri|ken| an",
    "srp": " pr| i |rav|pra| na|na | sv|ma | po|je |da |ima|a p|ja |a i|vo |nje|ko |va |anj|ti |i p|ako| u |a s| da|avo|ju |i s|ost| za|o i|sva| im|vak|o n|e s|ava|nja| sl| ko|om |no | ne|ne |li |u s| dr|ili|a n|slo|obo|koj|ih |lob|bod|im |sti|stv|a o| il| bi|pri|a u| ra| je|og |jed|enj|e p|u p|van|ni |a d|i u|edn|iti|nos|a k|o d|ova|pro| su|i i| os|ran|sta|dru|e i|cij|se |rod| ob|i o|aju|e o|i n|ove| de|aci| ka|ovo| ni| od|ve | se|i d| st|m i|voj|avn|uje|eni|ija|dna|žav|u i|red|su |nov|odi|tva|e b|oja|što|lju|u o|ara|a b|ji |drž|rža|odn|jan|nim|poš|ošt|a j|ka |jen| ov|u u| nj|du |ave|osn|sno|šti|aro|raz|bit|a z|u z|de | iz|nih|o p|u d|e d|pre|vu |u n|lja| tr|tu |eđu|nar|gov|svo|bez|ičn|lje|e n|za |vno|lo |oji| sa|to |an |nak| me|čno|đen|vim|nac|oda|ani|me |iko|nik|ika|e k|pos| kr|tre|reb|nst|stu|e u|ku | do|ašt|tit|aln|dno|jeg|nom|olj|nog|m s| vr|o k|oj |čov|ans|ovi|o s|bra|te |tav|tup|eno|živ|zaš|em |i m|dni|šen|var|riv|rug|vol|avi|tan|štv|kao|ao | li|st |ilo|nju|sme|o j| sm| čo|odu|vre|dst|od |a t|kri| bu|bud| ve|ver|a r|m n|del|tvo|međ|oje|jem|m p|avl|vlj|ego|u v|pod|ena|ugi|la |jav|por| op|ruš|ušt|kom|edi|ba |kla| mo|oju|i b|kak|akv|rad|nu |vek|rim|gla|juć|ude|vni|eba|e r|svi|i v|itu|ter",
    "ckb": " he| û |ên | bi| ma|in | di|na |maf|an | ku|ku | de| ji|xwe|her|iya| xw|ya |kes|kir|rin|iri| ne|ji |bi |yên|afê|e b|de |tin|ke |iyê|e h|es |ye | we|er |di |we |i b|ê d|erk| na|î û| an|ina| be|yê |eye|rke|î y|nê |kî |diy|ete|hey|hem| ci|eke| li|wek|ber|fê |n d|li | bê| se|yî | te|ne |rî |sti|net|tew|yek|af |hev|yan|n b|kar| ki|re |e k|wî |i h| ew|n û|û b|aza|n k| wî| mi|î b|dan|e d|î a|ekî|a m| az|zad|mir|iro|rov|nav|n h|ser|est|a w|ara|bin|ewe|anê|adi|tê |be |emû|mû | yê| ya|ta |ast|tî |ev |ewl|s m|n m|wey| tu|wle| bo|bo | tê|n j| me|ê h|din|ras|î d|n n| da|n w|bat|wed|ema|ê b|cih|st | ge|iye|ing|ar |în |r k| ke| pê|îna|î h|ekh|khe|vî |ana|par|û m|ûna|civ|vak|n e|dî |nge|geh| ye|ê t|ê k|û a|fên|hî |e û|av |eyî|bûn|erb|î m|bik|ê m|a x|iva| re|e n|eyê|vê |ane|man|nên|ela|end| pa|erî|n x| ta|jî |ika|kê |a k|f û|f h|e j|î n|ra |ehî|tiy|tên|û h|a s|rbe|bes|mî |ari|eza| ni|nek|n a|ov |i n|erw|rwe|erd|aye|e e|riy| a |ike|ê x|ovî|û d|inê|etê|tem|yet|eta|ek |ê j|a n|e t|i d|zan|bê |anî|nîn| ra|ama|ere| hi|i a|tuk|uke|bib|lat|awa|u d|ibe|xeb|atê|i m|mal|nda|ewa|a d|a b|arî| ba|edi| hî|hîn|rti| za|ist|e m| wi|mam| şe| qa|qan|anû|nûn|asî|han| ên|a g|u h|tu |dew|let|are|ine|pêş|êr |e a|wel|ger",
    "yor": "ti | ní|ó̩ | è̩|ní | lá|̩n |o̩n|é̩ |wo̩|àn | e̩|kan|an |tó̩| tí|tí | kò|ò̩ | àw|̩tó|è̩ | àt|è̩t|bí |e̩n|àti|lát|áti| gb|lè̩| ló| ó |s̩e|àwo|gbo|̩nì|n l| a | tó|í è|ra | s̩|n t|ò̩k|tó |sí |kò̩|̩ka|o̩ | sí|ìyà|orí|ílè|ì k|̩‐è|dè |yàn|ni |̩ à|ún | or|èdè|jé̩|ríl|è̩‐|‐èd|í à|àbí|n à|nì |ò̩ò|̩ò̩|í ó| tà|tàb| ti|̩ t|jo̩|̩ l|̩e | wo|í ì|nìy|ó n| jé|ló | sì|kò |n è|wó̩|n n| bá|̩ s|rè̩|sì | fú|fún|í a| pé|̩ni| òm| kí|gbà| èn|ènì|pé |in |ba |òmì|nir|ira|ìí |ràn| ìg|ìgb|n o|bá |mìn|ìni|gba|kí |n e| rè|e̩ |̩ n|un |̩ p| o̩|í ò|nín|nú |fi |gbé|yé | ka|ínú|a k|bé̩|mo̩| fi|̩ ì|í i|ó s|i l|wà |o̩d|̩dò|dò̩|̩ o|bò |dá |i ì|bo̩|hun|i ò|o̩m|̩mo|̩ k|áà |̩wó|bo |àgb|ló̩| ò̩|ó j| bí| oh|ohu|í k|n s|írà|bà |ara| ìb|ogb|pò̩|ú ì|yìí|rú |kó̩|ó̩n|i t|̩ ò| lè|lè |̩ e|à t|à l|bog|a w|gé̩| yó|yóò|óò |ò l|̩gb|ò̩r|̩ y|í w|̩ f|í t| wà|ó̩w|yí |ó b|̩ a|ìké|i n|i è|láà|àbò|fin|wùj|ùjo|n k|í e|é̩n| òf|òfi| mì|mìí|ìír|jú |rin|̩é̩|i j|ó t| ar| ir| ná|náà| ìk|̩ b|i s|ú à| yì|kàn|irú|rí | i |è̩k|̩kó|fé̩|é à|i o|s̩é|̩ m| ìd|è̩d|̩dá|i à|àwù|à n|ú è|wù | èt|áyé|í g|í o| mú|a l|láb|ábé|̩è̩|ìn | kú|láì| àà|i g|bé |níp|ípa| ìm|níg|ígb|wò̩|báy|ké̩|mú |í n|de |è̩s|mó̩| dá|i a|dó̩|ó̩g| ni|i p| wó|ayé|ùn |̩ w|a n|n f|n ì|je̩|ò t|n g",
    "uzn": "lar|ish|an |ga |ar | va| bi|da |va |ir | hu|iga|sh |uqu|bir|shi|huq|quq|gan| ha| bo|ini|ng |a e|r b|ni | ta|lis|lik|ing|ida|oʻl|ili|ari|nin|on | in|ins|adi|nso|son|iy | oʻ|lan| ma|har|dir|hi |kin|ash|i b|boʻ| yo| mu|uqi|dan|ila|qig|ega|r i|qla|oʻz| eg|a b| er|erk|kla|qil|oli|ki |gad| ol|nli|lga|at |i h|a o|rki|oki|osh|lig|igi| qa|yok|ib |las|n m| ba| qi|n b|ara|atl|ri |iya| sh|ham|ala|lat|bil|in |r h|bos|a t|siy|a y|cha|n o|a h|ik |a s|inl|a q|yat|nis| et|eti|osi|h h|i v|ani|tla|til|mas|ʻli|asi|ati| qo|i m|ris|im |a i|uql|arn|rni|qar|ha |gi | da|sha|n h|i o|rch|mla|li |i t| xa|arc|bar|ʻz |hun|a a|rla| bu|a m|lin|lib|taʼ| tu| as|h v|tib|aro|un |tga|r v|ikl| be|mum|n q|ali| te|sid| to|mat|amd|mda|tas| ke|shu|lla|shg|hga|n e| ya|dam|aml|oya|xal|hla|ola|iri|irl|ill|rga|iro|tis| de|umk|mki| eʼ|ten|eng|rda| is| sa|gin|imo|tar|ush|ur |ayo|rak| so|alq| ki|aza|k b|oda|oʻr|a k|tni|ʻlg|n v|hda|nda|shq|hqa|zar|miy|i q| mi|mil|r t| si|ak |ada|rat|or |kat|era|siz|am |ch |aso|sos|yot|atn|shl|n t|nid|y t|ana|ti |rin|asl|bu |sin|dav|ilg|as |i y|ech|nga|lak|h k|ino|ʼti|gʻi|muh|a v|bor|uch|lim|a u|uni|lli|n i|uri|si |i e| ka| ch|a d| ja|ami|qon|na |rig|lma|ker|avl|vla|i a|dek|ekl|liy|aka| em|ema|eʼt",
    "zlm": "an |ang| ke| se|ng | da|ada|dan|ara| pe|ak | be|ran|ber| me|ah |nya|hak|per|n s|ata|ala|ya |a s|kan|asa|lah|n k| di|da |aan|gan|nga|dal|erh| ma|n d|eba|rha|a p| ha|kep|pad|yan|ap |ama| ba| ya|nda| te|ra |a b|tia|man|eng|a d|ora|men|iap|n p|ter|epa|san| or|eti|pen| ad| at|n a|a a|h b|set|tan|sia|tau|n t|n b|ta |dak| sa|sa |at |au |ela|apa|pa |beb|bas|p o|h d|n m|keb|end|aka|ega|a k|am |sam|ana|gar|k k|ban|ole|leh|neg| un|lam|di |g a|eh |n y|aha|han|a m|eri|any|ma | ti|a t|uan|mas|ngs|atu|seb|ebe|ing|ian|car|bag| ne|mem|kes|mat|gsa|ia |ika|i d|nan|asi|und| la|epe|ert|agi|emb|na |ers| de|emu|pem|ngg|anu|i m|ind|erk|ung|n h|tu |gi |kla|g b|pun|iha| in|nus|usi|tin|al |si |as |akl|dap|erl|era|sec|eca|i s|lan|bar|k m|ena|aya| as|sas|nny|rta|sem|awa| su|bol|rat|den|ini|ni | ta| he|hen| an|tar|g m|ai |kem|adi|had|in |ti |i k| bo|uka| ap|g t|ka |ann|ema|g s|ain|k h|rka|ri |n i|aga|un |ses|dun|enu|emp|elu|lai|kat|ent|nta|rsa|iad|ua |dia|ira|a n|mpu|ntu|uk |mel|k d|har|ker|dil|mar|h m|aja|ila|k a|mba|lua|i a|rak|uat|jua|rma| pu|t d|rga|i p|uma|ser|esi|ar |esa|nak|bah|rli|lin|ndu|dar|ari|ese|l d|ant|ngk| ol|sua|g d|ati|tuk|erm|saa|erj|rja|kea|raa|gam|g u|tik|ida|sek|eka|sat|i b|mbe|unt|dir|iri",
    "ibo": "a n|e n|ke | na|na | ọ | bụ|nwe| n |ere|ọ b|re |nye| nk|ya |la | nw| ik| ma|ye |e ọ|ike|a o|nke|ụ n|a m| ya|a ọ|ma |bụl|ụla|e i| on| a |iki|kik|ka |ony|ta |bụ |kwa|i n|a i| nd|di |a a|wa |wer|do | mm|dụ |e a|any|ha | ga| ok|e m| ob|he |ndi|e o|a e|ite|ọ n|rụ |hi |ga‐|mma|wu | dị|aka|ara|che|oke|o n|we |n o| ih|mad|adụ|obo|bod|odo|a g|te | ez|hị | ka|ụta|be | an|akw|zi |a‐e|dị | oh|gba|nya|u n|ihe| ak|me |i m|ala|ri | ọz|ghi|ohe|her| mb|ba | nt| si| iw|weg|pụt|ra |iri|chi|ụrụ|rị |zọ |oro|ro |iwu|a‐a|ụ ọ|ụ i| eb|ebe|e ị|a y| in|ezi|kpa|kpe|inw|mba|sit|ịrị|ile|le | ha|e e|bi |n e|chị| en| e |i ọ|asị|mak| ni|nil|ghị|si |ide|a u|o i|i o|i i|apụ|a s|e y|u o|ụ m|ahụ|hụ |a ụ|nkw|edo|n a|ru |ụ o|ozu|enw|ọzọ|kwu|gid|sor|egh|yer|tar|n i|pa |eny|uru|kwe|toz|ị o| mk|ama|de |uso|tu | im|ime| me|i a|ịch|ọ d| ịk|obi| ọn|hed| ọr|ọrụ| to| ch|gas|wet|mkp| kw|osi|a d| nh|nha|ọnọ|nọd| al| nc|nch|sir| o |n u|sịr|eta|u ọ|ị m|n ụ| us|nọ | ot|otu| gb|ọdụ|nwa|o m| ag|ali|lit|ọta|ega|ị n|e k|e s|ji |a k|ikp|ọch| ug|ban|ekọ|pe |nta|agb|na‐|n ọ|hu |i e|e g|a b|zu |chọ|u a|kwụ|ram|esi|uch|onw| nọ|ị k|u m|eme|wan|e h|dịg|ịgh|hịc|ugw|gwu| di|ich|cha| og|okp|kpu| nn|zụz|zụ |i ị|o y|ach| ng|pụr|ị e|a h|meg|nat|uwa",
    "ceb": "sa | sa|ng |ang| ka|an | pa|ga | ma|nga|pag| ng|a p|on |kat|a k|od |ug | ug|g m| an|ana|n s|ay |ata|ung|ngo|a m|atu|ala|san|ag |tun|g s|g k|d s|god|a s|ong| mg|mga|g p|n u|yon|pan|a a|usa|ing|tag|tan|una|mat|ali|aga|g u|han| us|nan|y k|man|ina|non|kin| na|lan|syo|a b|asa|nay|a i|n n| ta|awa|nas|taw| o |nsa|gaw|a n|agp|dun|iya|ban|isa|was| ad|adu| gi|ili|ini|asy|bis|nag|g a|a t|o s| bi|nah|lin| ki|al |sam|lay|ahi|nal| ba|ano| wa|wal|asu|agt| di|yan|ama|a u|n a|ags| iy|gan|n k|kan|him|kag|ya | un|gpa|kas|aha| su|g t|awh|wha|gsa|l n|agk|gka|a g|gla|kal|gal|ran|sud|ud |imo|d u|aba|aka|ika|ig |ngl|ipo|g d|ara|g n|uka|uns|uta|d n|og |i s|y s|kau|li |n o|aki|o p|kab| og|ot |mta|gik| si|n m|gpi| la|g i|aho|ayo|iin|ok |awo|hat|o a|gon|ip |a w|apa|lip|na |a h|bal|la |ad | ga| ti| hu|uba|wo |ati|uga|hon|hin|ma |sal| ub|agb|gba|nin| bu|buh|uha|t n|ahu|may|pin|as |ni |mak|ihi|abu|uma| in|say|d a| pi|dil| ni|ins| pu|agh|at |hun|but|aug|lak|bah|sak|o u|mal|s s|os |amt|t s|pod|sos|ngp|lam|aod|ila|a d|ami|k s|tin|ura|mo |agi|tra| at|bat|aan|ulo|iha|ha |n p|kar|oon|sya|ona|aya|in |inu| hi|it |agl|amb|mbo|mag|a l|ho |lao| al| il|iko|ngb|mah|lih|g b|gta|gtu|y p|rab|ato|tar|nab| re| so|osy|yal|aw |gda",
    "tgl": "ng |ang| pa|an |sa | sa|at | ka| ng| ma|ala|g p|apa| na|ata|pag|pan| at| an|ay |ara|ga |a p|tan|g m| mg|mga|n n|pat| ba|aya|n a|na |ama|g k|kar|awa|a k|lan|rap|gka|nga|n s|g n|g b|a a|aha| ta|agk|gan|asa|tao|aka|yan|ao |a m|may|man|kal|ing|nan|a s| la|aga|ban|ali|g a|ana|y m|kat|san|kan|pam|g i|ong|mag|a n|baw|o a|wat| y |isa|lay|y k|t t|ila|g s|in |kas|o y|aan| ay|ina|t n|t p|wal|ag |una|yon| it| o |nag|tay|pin|ili|ans|lal|ito|any|nsa|kak|a i|lah|mam|nta|nya|gal|hay|to |ant|aba|ran|agt|on |han|agp|kap| ga|t s| wa|gaw|o m|ya |as |g t|lip|y n|hat|g l|ung|ngk|no |gpa|lag|gta|t m|wa |yaa|ari|sal|a l|kai|pap|lin| pi|ita|ahi| is| di|agi|ipi|bat|mak|pun|a b|y s|aki|tat|la |hin|pah|yag|gay|o n|ags|iti|di |i n|sas| si|t a|al |a t|ika|mal|s n|ipu|t k|ais|hal|i a|sap|lit|od |ihi|alo|y p|ani|ig |par|ap | ip|tas|gin|gga|nin|uma|gsa|ano|ira|a g|nah|uka|syo| bu|ini|o s|nap|ngi|o p| ha|a h|mah|a o|li |ipa|uha|asy|lam|iba|aru|mba|g e|usa|lim|sam|pil| da|kin|duk|sin|dai|aig|igd|gdi|dig| tu|d n|ba |nas|pak|iga|kil|n o|nak|ad |lig|git|lab|ma |kab|nar|tag| ed|edu|aho|buh|and|nda|mas|pas| ib|it |ri |lun|ula|agb|g g|ain|pul|ino|gi |sar|g w|abu|s a|api|nil|iyo|siy|iya|anl|uli|aun|agg|amb|g d|ati| li|i m",
    "hun": " sz| a |en | va|és | és|min|ek | mi| jo|jog|ind|an |nek|sze|ság| az|gy |nde|ala|sza|den|az |a v|val|ele| el|mél|oga|egy|n a| eg|ga |zem|zab| me|emé|van|int|tel|aba|bad|tet|ak | te|tás| ne|gye|t a|ény|ély|tt |n s|ben|zet|ség|lam|meg|nak|ni |ete| se|lyn|yne|s a| al|let|z e|ra |et |agy|sen|eté|k j|tek|mel|kin|ok | ki|ez |hoz|oz |n m|re |vag|ett|emb|mbe|es | kö| le|nem|ell|em |ely|hog|ogy|s s|gok|atá|k a|nt |köz| ho|z a|hez|el |len|yen|ásá|ads|dsá|tés| em|a m|a s|nte| ál|k m|ás |a t|szt|áll|a h|y a|ogo|sem|ber|ban|enk|nki|nye|lap|t s|ese|ame|nyi|k é|ágo|ló |ág |t m|on | vé|i m|ami|ébe|s e|lat|lla|ly |mén|fel|tat|eri|lő |a n|eki|n v|yes|emz|mze|lle|a a| cs|át |kül|elő|l a|nd | ke|ég |i é|lis|vet|éte|ért|rés|yil|ésé|enl|szo| am|tar|art|alá|elé|a j| ny|koz|het|ész|ja |lem|nlő|ri |i j|s é|éle|ól |aló|kel| ha|ges|zás|más|s t|tár|s k|t é|vel|tko|zés|ése|se |tő |ot |ott|sít| fe|n k|lek|tte|olg|áza|ssá|e a|eve|szá|ti |n e|ül |zte|ána|zto|tos|árs|os |k k|eze|leh|ehe|t n|tes|kor|atk|del|t k|tot|ány|ége|fej|i v| né|ház|leg|k e|ll |nev|is |ába|t t|véd|éde|d a|zer|ere|kif|ife|téb|ny |ai | bi|biz|izt|i e|sül|lt |zat|at |elm| ar|arr|rra|sel|t e|ágá|s n|csa| mu|nél|it |esz| há|zas|ass|üle| ta|nyo|alk",
    "azj": " və|və |ər |lar| hə|in |ir | ol| hü|hüq|üqu| bi|quq|na |lər|də |hər| şə|bir|an |lik| tə|r b|mal|lma|ası|ini|r h|ən |şəx|əxs|qla|arı|a m|dir|ali|aq |uqu| ma|ilə|ın |una|yət| ya|ara|ikd|ar |əri|əsi|r ş|rin|əti|dən|nin|n h| az|yyə|sin| mü|tin|ni |zad|iyy| mə|ərə|mək|ün |nda|ət |i v|ını|nın|ndə|ə a|aza|rın|qun|olu| et| qa|lıq|ilm|kdi|lə |ə y|ək |lmə|ına|ind|olm|xs |mas|sın|lun| in|ə b|da |n t|əmi| bə|adl|dlı|n m|nə |q v|ya |tmə|bər| on|ə h|əya|sı |nun|etm|dan|inə|maq|un |raq|ə v| va|n a|n b|rlə|ə m|si |əra|n i|ınd| öz|anı|nma|ama|rı |ığı|li |il | al|ala| di|ə d|ik |irl|ins|lət|a b|bil|ıq |müd| sə|ə i|nı |nla|ələ|dil|alı|ə e|unm|n v|ola|asi|üda|ili| də|nsa|san|uql|ə o|xsi| he|uq |ətl|həm|əni|eyn|u v| da|tər|min|yin|kil|dır| bu|lan|iş | ha|məs| ki|mi |lığ|ə q|q h|i s|daf|afi|fiə|r v| iş| əs|sos|osi|sia|xal|alq| ta| as| ed|bu |heç|eç |rə |yan|ı h|kim|iyi|ı o|ina|siy|əsa|sas|a q|yar|lı |tün| is|ist| so|al |n ə|ifa|əmə|ə t|mil|ill|lıd|ıdı|ır |ədə|ıql|liy|tlə|a h|məz| bü|büt|ütü|iya|iə | üç|üçü|çün|t v|dax|axi|xil|r a|ılı|man|sil| se|seç|adə|ial|onu|öz | cə|miy|əyi|n e|edi| mi| nə|a v|mən|ril|əz |ild|rab|abə|şər|ğın|aya|zam| ni|ulm| xa|təh|əhs|hsi|i h|sti|qu |var|ad |tam|uğu|z h|qan|rəf|n d",
    "ces": " pr| a |ní | ne|prá|ráv|ost| sv| po|na |ho |ch | na|nos|o n|ání| ro|ti |vo |neb|má |ávo| má|ou | ka|kaž|ažd|ebo|bo | je| za|ždý|dý | př|svo|a s|sti| st|á p| v |vob|obo| sp|bod|pro| zá|rod|ých|ván|ý m|né | by|ení|ého|spo| ná|í a|ová|o p|roz|mi |ně |ter| li|a p|nár|áro| ja|jak|by |to |lid|u p| vš|ny |ím |í p|i a|a z|o v|kte|mu |at |odn| vy| ma| so|ví |zák|tní|a v|oli|li | kt|í n|kla|do |je |pod|en |em |byl|mí |áva|stá| do|t s|rov|í s|tví|vše|it |dní|o s| ve|pol|í b| bý|být|ýt | se|čin| k |sou|a n|stn|ran|vol|nou|ejn|nes|se |ci |nýc|du |ným|stv|žen|své|vé |ají|jeh|eho|va |mez|ním|ích|ým |ké |ečn|pří|u s|tát|i s|kol|ova|e s|ech|í v|ids| i |maj| to|nu |hra|ave|ole|i v|kon|m p|ému|y s|o z|eré| ze|o d| že|chn|ovn|len|dsk|lad|vat|chr| ta|m a| ab|aby|sta|pra|néh|esm|smí| ni|i n|že |ako| os|sob|aké|i p|st |rac|kdo|zem|m n|odu| ji|bez|ste|ákl|ens|ými|í m| vz|i k| oc|och|jí |oci|áln|lní|a m|dy |lně|vou|při|rav|leč| s |t v| či|čen|áv |slu|jin|oko|nez|tej|řís|stu|ské|ský|nit|ivo|a j|věd|iál| me|ezi|ven|oln|zen|í z|y b|zac|níc|ky |u a|a o|u k|inn|est| tr|svě|nik|ikd|í k| mu|u v|kéh|jno|jíc| dů| od|tup|ože|i j|odi|děl|ího|rok|anu|soc|ciá|ve |é v|něn|din| vo| pl|pln|vin|u o|h p|tak|adn|a t|cho|ává",
    "run": "ra |we |wa | mu|e a| n |se |a k|ira|ntu|tu | ku| um|ko |a i|mu |ye |hir|iri|mun|ing|unt|ere|ash|shi|a n|umu|zwa| bi|gu |ege|a a|za |teg|e k|ama|go |aba|uba|ngo| ba|o a| ar|ung|ish|ora|a m|e n| we|sho|na |ese| kw|nga|e m|mwe| ab|ugu|ate|ndi|kwi| gu|ger|riz|wes| at|di |u w|n u|yo |gih|ban|ngi|iza|e b|ara| am|ri |ka |a b|e i|hob|obo| ca|ro |u b|can|nke|ezw|a u| in|bor|bah|ahi|rez|iwe|gir|iki|igi|ihu|ke |ari|ang|aku|a g|hug|ank|ose|u n|o n|rwa| ak|and|kan| vy|ngu|nta| ub|aka|ran| nt|n i|ata|kur|kun|i n|ana| ko|e u|iye| ka|re |any|amw|ta |nye|uko|gin| zi|ite|era|ga |aha| ib| ng|n a|o u|o k| iv|ivy|ho | as|sha|o m|o b| bu|mak|ako|o i| ig|o z|o y| uk|ubu|aga|izw|i b|vyi|ba |aho|kir|nya| is|kub|hin| it|uri|gan|rik| im|u m|guk|bat|nge|kug|ani|vyo|ene|imi|imw| y |jwe|ze |agi|e c|u a|gek|ush|i i|uru|ham|uza|e y|ibi|amb|bur|ina|eme|i a|abi|ha | nk|eye|gus|ber|u g|no |rah|zi |w i|ma |tun|ind|ron|ras|wo |ne |wub| gi|gen|kiz|y i|kor|ura| zu|zub|zin|je |iro|mat|eko|bwa|ika| bo|bak|onk| ma|ugi|mbe|ihe| mw|eka|ukw|wir|ryo| ic|a z| ry|bwo| ag|yiw| ki|gis| yo|bik|ni |nka|rek| bw| ya|tse| ha| ah|umw|he |eng|bir|aro|ury|twa|ant|a c|tar|uki|mw |bih|ku |tan|bos|nde|uro|y a|utu| no|i y| yi|ya |puz|zam|eny",
    "plt": "ny |na |ana| ny|y f|a n|sy |aha|ra |a a| fa|n n|a m|y n|an | fi|tra|any| ma|han|nan|ara|y a| am|in |ka |y m|ami|olo| ts|min|lon| mi| sy| na|a t| ol|fan|a i| ha| iz|iza|man|ina|ona|aka|y h|ian|o a|a h|reh|a s|etr|het|a f|on |ire|fah|tsy|mba|ay |zan| hi| ar|ndr|ira|y o|y t|ehe| an|o h|y i|afa|ren|ran| zo|ena|dia|amb|amp|ala|zo |ika|y s| di|tan| az|y z|m p|rin|ia |n j| jo|jo | dr|a d|zy |ao |ry |and| ka|dre|mpi|rah|nen|haf|n d| ir|eo |elo| ta|omb|rai|oan|fa |am | pi|ene|ho | ho|ant|iny|itr|azo|dra|ava|tsa| to|tsi|zon|asa|van|a k|ari|ha |n i|mbe|ray|fia| fo|sa |ony|isy|ova|lal|ly |azy|o f|bel|lom|ham|mis|sam|zay| ra|oto|fir|ban|a r|nat|kan| vo| he|ito|ary|nin|iha| re|a e| ko|tok|fit| no|ita|iar|fot|nam|voa|isa|y v| sa|y r|o n|no |aly|mah|har|ain|kam|aza|n o|otr|eri|hev|oka|sia|ial|atr|y l| la|ila|oa |y d|ano|ata|its|tov|pia|y k|pan|fam|oko|aro|nto|pir| ao|ty |anj|nja|reo| as|o s|hia|o t|mpa|mit| eo|ais|sir|air|ba |tin| it|ver|ino|vah|vy |ton|tao|ank|era|rak|kon|a z|tot|ive|ame|aho|hoa|hit|ati|ity|o m|mik|a v|ani|ori|koa|hah|nga|dri|eha|dy | mo|oni| za|ato|bar|jak|n t|nao|end|eve|lah|aov|mia|izy|lan|nar|ria|ama| pa| mb|aln|lna|ifa|za |to |dro|va | in|ind|ehi|n k|iva|nta| va| al|via|rar",
    "qug": "una|ta | ka|na |ka |ash|cha|a k|ari|ish|kun|kta|pak|ana|hka|mi |shk|apa|ach|hay|akt|shp|man|ak | ch| ha|ata|rin|lla|tak|ita|ami|ama|aku| pa|har|pas|ayñ|yñi|ina| ma| ru|uku|sh |hpa|run|kuy|all|aka| tu|tuk|an |chi|yta|a c|chu|in |ñit|ris|a h|nka|nak|tap|kan| ki|ayt|pi |pa | sh|i k|nap|a p|pay|kaw|kam|nam|ayp|aws|wsa|a s|ank|nta|iri|uy |a t|hin|a m|ay | li|ant|kay|lia|nat|a r|shi|iak| wa|lak|uya|say|yuy|y r|ypa|kis|a a|hun| yu|n t|tam| ti|n k| ya|yay|lli|a w|hpi|api| al|un |yku|ipa|a i|iku|ayk|shu| sa|ush|pir|ich|kas|kat| il|huk|ill|a y|hu |rik|yac|a l|kac| ku|hik|tan|ypi|wan|ika|i c| ni|ima|ila|ink|ayl|yll|mac|nis| ta| wi|kus|i y|i p|n s|llu|tin|la |yan|kpi|awa|li | ri|may|tik|iks|lan| pi|aya|kin|yas|ksi|kll|kak|lat|aym|ura|war| ay|k h|uch|akp|sha|ukt|nch|h k|i t|ull|uma|mas|iya|kir| ii|h m|pip|n p|kik|iki|i s|kar|aki|riy|han|y h| su|mak|n m|tac|nal|nac| ña|k k|k t|k a|iwa|mam|i m|nki|yma|wil|his|pal|i i|asi|nmi|i w|sam|k l| hu|sum|pam|kap|k i|pan|iia|huc|ik | mu|mun|pik|was|k m|ma |hat| im|k r|akl|u t|ha |llp|a u|wak|has|anc| ak|imi|mal|y k|ian|iña|tar|yka| iñ|iñi| mi|n y|ywa|uyk|unk|a n|arm|rmi|h p|pur|akk|kim|san|ati|uti|uri| ar|sak|i a|hap|iyt|ayw|si |yar|las|lpa|ñaw|awp|wpa|i r",
    "mad": "an |eng|ban|ng | sa| ka|dha| ba|ren|ak |ang| se| ha|hak| dh|na | pa|adh|se |a s|aba|n s|ara|ngg|are|ha |aga|sa | or|sar|ore|asa|ana| ma|a k|aan|gi |ale| ag|gad|a b|n o|n k|ra |ala|eba|gan| ke|dhu|aja|ota|bas|man|dhi|n b|tab|ka |sab|ama|beb|abb|at |nga| ta|ggu|ako|pan|huw|uwi|wi | ot|san|a d|ata|eka|i h|bba|agi|ba |lak|hal|ong|kab|em |g a|lem|a o| pe| na|par|ane|ngs|nge|gar|a p|tan|gsa|a a|ran|ken|i s|guy|uy |k h|n p|n a|ada|al |apa| ga|on | an|g s|ta |kaa| e |e d|pon|nek|ssa|a m|kal|a e|e e| la|kat|ona|abe|nan|asi|jan|ate|lab|ri |sal|lan|i p|sad|aka|e a|a h|ari|ena| bi|oss|si |daj|i k| ng|har|gen|ton|e k|epo|ano|bad|car|n d|ar |era| be|nag|kon|g k|ase|nya|nos|n n|mat| kl|mas|ela| da| al|n t|uwa|wan|sae|pad|ggi| so|as |hi |adi|a n|i d|g e|k k|ne |oan|uan|k s|k a|e b|ah |ina|kla|ter|om |gap|le |koa|yat|per|neg|ega| ja|bi |abi|aha| ep|aon| as| mo|n h|i a|one| di|ma |kas|m p|di |aya|nto|int|n e|te |bat|epa|nda| ca|pam|e s|amp|to |dra|ann|oko|rga|nna|e p|g p|nta| ra|and|i b|nao|k d|pen|aen|ste|ila|yar|a t|mpo|ok |set|n m|k b|isa|kom|raj|arg|ika|bin|ant|ga |hid|idh|aju|i m|nas|kar|mos|ost| ho|lae|dil|t s|a l|das|rek|tad| a | po|ett|tto| to|bis| dr|jat|add| ko|ent|gam|e m|ndh|hig|iga|maj",
    "nya": "ndi|ali|a k|a m| ku| nd|wa |na |nth| mu|yen| al|ra |thu|se |hu |nse|di |a n|la | pa| wa|mun|unt|nga| la|a u|u a|e a| ma|za |ons|ace|ce | lo|iye|a l|idw|ang| ka|kha|liy|ens|li |ala|ira|pa |ene|i n|we |e m|era|ana|dwa|lo |hal|ulu|ko |dzi|iko|yo |o w| ci|a p|ga |chi| mo|o l|lu |o m|zik| um|moy|oyo|ufu|ner| an|and|iri|umo|ka |a a|dan|ena| uf|ful| nc|nch|hit|ito|to |a c|kuk|dwe| da|fun|wac| dz|e l|kap|ape|a z|e k|ti |u w|ere| za|lir|pen|aye|tha|kut|ro |mu |lid| zo|ofu|ing|i m|amu|mal|o c|kwa|mwa|so |o a|o n|i p|eza| mw|nso|iro|zo |i d|lin|ri |edw| a |i l| li|a d|kul|ati|uti|una|lan|i k|o k|ung|alo|dza|i c|o z|a b|uni|iki|lam|mul|ulo| ca|nkh|nzi|gan| na|ant|e n|san|tsa|wir|oli|u k|lon|dip|ipo|unz|yan|gwi|ca |ome| ko|aku|akh|pon|ngw|kir| po|uli|gwe|cit|mer|pan|kup|ame|mba|tsi|bun|ukh|ope|siy|iya| ya| am|han| bu|ama|bvo|vom|rez|lac| kw|men|u n|ao |pez| on|zid|osa|u o|i a|nda|e p|ne |ank|hun|o o|nik|ku |its|adz|u d|aka|diz| kh|ina|ezo|ndu|kho|okh|ya |awi|izo|ans|pat|eze|khu|zi |phu|kus|eka|o p| ad|mol|ets|sa |iza|kwe|wez| un|izi|oma|ma |oci|du |ula|ani|lok|haw|ika|ja |say|nji|jir|amb|ats|sid|mai|aik|mak|aph|i u|isa|lal|u m|ogw|no |oye|ukw|osi|sam| si|win| zi|ni |tse|si |e o|opa|emb| ba|ban",
    "zyb": "bou|aeu|enz|nz |eng|iz |ih |uz |uq |ing| bo|oux| di| ca|z g|dih|ux |ngh|cae|gen|euq|z c|ng |you|ung|ngz|ij | mi| gi|miz|aen| ge|z d| ci|gya| yi| de|ouj|uj | gu|cin|ien|ngj| mb|mbo|dae|zli|gij| se|j g|ang|z y|j d|ouz| cu| ba|nae|h g| da|yin|oz |de |z b|nzl|li |nj |x m|euz| cw| yo|iq |gz |q g|x b|yau|h c|vun|inh|ix | ga|cwy|wyo| nd|vei|nda| ro|rox|oxn|z m|i c|j b| si|wz |gh | gy|cun|gue|xna|unz|hoz|can|bau|ei |z h|yen| li|inz|dan|q c| hi|gj |uh | vu|faz|yie| bi|zci|hin|goz|uek| fa|gun|aej|ej |ya |nh | ae| go|au |ciz|den|h m|nq |ngq|ouq|gva|z s| do|ci | wn|q d|eix|h d|ekg|kgy|q s|hu |u d|j n|auj|j c|gai| ha|az |nhy|z l|gjs|jso|sou|ou |bin|sin|lij|h s|sev|eve|nei|q y|aiq|sen|h y| la|enj|ouh|i b|vih|din|q n|awz|j y|z r|enh|en |uen|bwn|wng|ozc|z n|anj|j s|liz|g g|g b|i d| ne|bae|awj|sei|eiq|hye|anz|oen|hix|zda|gak|ez |anh|u c|z v| ya|h f|x d|in |ghg|bie|enq|zsi|ghc|hci|siz|i g|n d|h b| du|cou|ngg|ngd|cuz|eiz| ho|dun|g c|law|j m| dw|env|nvi|dei|a d|ek |yaw|wn |giz|gzd|nzg|wnj|gda|ak |nde|auy|yuz|hgy| co|ujh|jhu|e c|hen|ujc|min|izy|g d|gzs|daw|aw |g y|ozg|ai |iuz|x l| na|iet|aih|gih|iuj|zbi|uyu|coz|sae|i m| he|zdi|dwg|q b| fu| ve|guh|iqg|qgy|yai|yoe| so|biu|vaq|aq |yun|izc| ra|cie|zge|n g",
    "kin": "ra | ku| mu|se |ntu|a k|tu |nga|umu|ye |li | um|mun|a n|unt|ira| n |ere|wa |we | gu|e n|mu |ko |a b|e a|o k|a u|a a|u b|e k|ose|uli|ro | ab|aba|gom|e b| ag|omb|ba |ugu|ang|o a|gu |mba| ib| ub|eng|ihu|za | bu|ama| by|hug| ba|o b|e u|kwi|ga |ash|ndi| ka|yo |e i|ren| cy| ak|iye| bi|re |ora|igi|gih|ban|ubu|di | nt| kw|gan|a g|aka|aga|nta|a m|iro|a i| am|ku |i m|ago|byo|ta |ka |cya|ibi|and|na |ali|uba|sha| bw|ili|yan|no |ese| ig|u m|o n|kan|ish|ana|sho|obo|era| we|ya |aci|i a|ura|wes|uko|e m|ran|o i|u w|uru|wo |kub|n a| im|ber|hob|bor|ure| no|ani|u a|gac|cir|o m|ush|bur|eke|ne |wiy|ara|nge|rwa|yos|e y| y |uga|bwa|ho |zo |ind|ane|mwe|iza|are|rag|ge |mo |bwo|bul|teg|ege|u k|u n|n i|ze |aha| uk|bye|anz| al| ki|bah|uha|ite|kug|gir|ngo|go |age|ger|u g|zir| ry|ugo|bih|akw|o g|guh|iki|bat|iby|gar|imi|mbe|y i|n u|ha |atu|mul|tan|eye|e c|kim| ni|shy|aho|tur|kir|ate|abo|je |bo | ng|u u|ata|o u|iko|gus| bo|bos| gi|a s|nir| ru|gek|i b|eza|i n|nzi|i i|rez|kur|ako|any| as|ung| se|bis|nya|o r|uki| ya|ngi|mat|eko|ugi| in|o y|kor|imw|rer|bak|yam|bit| ik|kar|ire|ige|shi|hin|ing|byi|nu |mug| at|yem|eme|gaz|irw|yer|rek|key|ihe|gen| ic|icy|hak|but|ets|tse|eze| ur| na|bag|awe|ubi| yi|i k|ezo|tek|ubw|rya|uza",
    "zul": "nge|oku|lo | ng|a n|ung|nga|le |lun| no|elo|la |wa |e n|ele|ntu|tu |gel|we |ngo| um|e u|thi|uth|ke |hi |ni |ezi|lek| ku|nom|ma |o n|onk|nke|pha|gok|a u|nel|ulu|unt|o l|kwe|oma|o e|ang|lul| uk|kul|a k|eni|uku| wo|kel|hla|mun| lo|ama| ne|ath|ho |umu|ela|won|elw|lwa|ban|a i|ule|zwe|ana| un|une|ing|lok|aka|elu|wen| kw|aba|tho|akh|khe|ala|gan|o y|enz|ko |thu|na |u u|a e|gen|i n|zin|kho|enk|kun|mal|alu|e k|lel| na|kat|e a|nku|eko|he |hak|lan|kwa| ez|o a|o o|kub|ane|ayo|yo |lwe|eth|obu| em|nzi| ok|okw|kut| ba|ile|ben|het|eki|nok|nye|ike|i k|so |isi|ise|esi| ab|mph|nhl| is|aph|fan|ga |isa|ini| ye|e i|nen|uba|ba |zi |hol|ka |ant| fu|fut|uhl|abe|and|do |ukh|kuk|eke|a a|kil|e w|the| ya|nda|za | im| in|olo|ekh|eli|ith|khu|eng|yok|nis|sa |kuh|o u|any|ye |e e|i w| ak|olu|ndl|a o| le|ne |ume|mel|eka|mth| ko|emp|isw|amb|emi|no |uny|iph|i i|zo |kuf|nay|ind|ezw|kuz|vik|alo|o w|hul|ebe|lin| yo|kan|eze|ndo|uph|hlo|yen|enh|phe|ufa|ake|ale|kug|fun|und|wez|li |seb|a l|ula|wam|din|ahl|nez|yez|nya|bus|bo |azw|o k|ink|kek|nan|i e|ola|izi|mbi|ili|han|kuv|ase|hel|hut|a y|kis|kuq|da |omp|swa|kup|nem|ano|phi| ol|azi|ubu|o i|kol|oko| el|e l|huk|ani|nje|sek|uke|lon|pho|kom|lak|kus|zis|ham|mba|izw|ulo|hun|i u|u n",
    "swe": " oc|och|ch |er |ing|för|ar |tt |en |ätt|nde| fö|rät|ill|et |and| en| ti| rä| de|til|het|ll |de |om |var|lig|gen| fr|ska|ell|nin|ng | ha|ter|as | in|ka |att|lle|der|und| i |sam|lla|fri|ghe|ens|all|ör |na |ler| at|ete|den| el| so| av|av |igh|r h|nva|la |r r|env|ga |tig|nsk|iga|har|t a|som| ut|tti|nge|t t|ion|a s|ns |a f| sk|a o|r s|män|an |är |isk|rna| st| si| vi| sa| al|t f|ra | be|a r| är| me|ati|n s|lan| va| an|med|tio|ern|nna|t e| un|äns|ta |nat|sta|ig | åt|ten|kli| gr|vis|t s| la|äll|one|änd|han| ge| li|ans|stä|ner|t i| må|gru|ver|rih|ihe| mä|sni|lik|n f| re|r a| na|må |ers|t o|ad |r e|da |det| vä|ent|run|rkl|kla|ri |h r|nom|kap|igt|gt |n e|dig|uta|tan|e s|dra|s f|ed |d f|lar|rin|ran|upp|erk|tta|ika|änn|r o|erv|rvi|kte|vid|a i|lag| på|g o|id |ari|s s|r u|lin| om|ro |a m|els|isn|del|sky|r d|e m|mot|ot |vil|på | mo|r m|str|örk|ndl|on |i o|nd |tni|n m|ber|nad|gan|örs|r f|kal|era|a d|dd |je |itt| up|sin|nga|täl|ras|n o|ärd|i s|r i|enn|a n|n a| hä|bet|ski|kil|n i|lse|rel|t b|g a|kyd|ydd|arj|rje|l v|s e|end|amt| fa|nas| så|inn|tat|per|t v|l a|int|tet|öra|e f|tra|r g|yck|r ä|vär|ege|arb|d e|re |nis|ap |ara|bar|l s|t l|lit|när|lke|h f|ckl|v s|rän|gar|ndr|mt |se |häl|h a|llm|lmä|ess|sa ",
    "lin": "na | na| ya|ya |a m| mo|a b|to | ko| bo|li |o n| li|i n| pe|i y|ngo|a n|a y|ki | ba| ma|kok|pe |la |a l|zal|oki|ali|nso|oto|ala|ons|so |mot|a k|nyo|eng|kol|go | ny|yon|nge|o e|ang|eko|te |o y|olo|oko|ma |a e|iko|e m|e b|lik|ko |o a|ako| ye|ye |ong|mak|si |isa| ek|aza|lo |sal|ama| te|o p|bat| az|e n|oyo|ani|ela|sen|o m|a p|ta |ban|i k|amb|ni | es|yo |aka|mba|osa| oy|mi |a t|eli|lis|i p|i m|ba |mok| to|mbo|bok|isi| mi|ing|lon|ato|o b| nd|ge |bot|ota| ez|nga|nde|eza|o t|kan|ka |gel|e k|bo |ese|sa |lam|koz|den|oba|omb| po|ga |mos|kop|oli|e e|yan|bon|oka|kob|lin|bik|po |kos| lo|sam|e a| ’t|’te|kot|ti |ngi| bi|e y|omi|esa|i b| el|elo|lok|gom|som|i t|ate|ika|kam|ope|a s|kat|ati|ata|wa |iki|i e|bom|tal| ka|oza|o l|bos|zwa|ola|pes| se|oke|bek|o o|ndi|bal|nda|nza|oso|omo|lak|bak|mis| at|bis|sus|usu|su |osu|lib|and|ozw|asi|ele|tel|mu |i l|e t|ase|mol|mob| nz|kel|ene|ne |mbi|ami|aye|nis|a ’|tan|le |obo|baz|pon|wan| ep|yeb|kum|sem|emb|mal|gi |nya|ote|e l|oku|bas| ta| ak| ti|tin|ina|gis|opo|ana|mab|bol|u y|mat|ebi|oti|mib|obe|a o|san| so|mbe|be | mb|ibo| et|ike|da | en|ben|za |yok|eni|tey|bwa|bi |kom|i o|gob|mik|umb|se |eba|e p|ibe|ale|lel|boy|eta|i a|bu |ime|sik|mon|ona|mel|ose|mwa|sol|geb|ebe",
    "som": " ka|ay |ka |an |oo |uu |da |yo |aha| iy|ada|aan|iyo|a i| wa| in|sha| ah| u |a a| qo|ama| la|ga |hay| dh|ma |aad| xa|ah |a d| da|qof|in |aa |iya|a s|a w| si| oo|isa|eey|yah|xaq| le|ku |lee|u l| ku|taa| ma|la |dha|ta |aq |q u|eya|y i|ast|sta|a k|ha |of | wu|wux|uxu|xuu|kas|sa |u x|ara|doo|wax| am|iis|ro |a q|inu|nuu|ala|a x|o a|maa|nay| sh| qa|o i| aa|kal|le | lo|loo|f k|o d|ana|a u|o x| xu| xo| ba| uu|yad|iga|a l|si |dii|a m|yaa|gu |ash|u d|ale|ima|adk|aas| ca|o m|do |lag|add|na |lo |o k|san| is| so|adi| mi| fa|xor|dka|aqa|iin| he|aar|had|rka|a o|ado|dad|soo|mid|kar|aro|baa|qaa| ha|nta|o h|ad |u k|aga|dda| ga|hii| sa|u s| ay|har|axa|mad|n k|eed|quu|haa|daa|o q|aal|o s|n l|xuq|uqu|n i|id |hel|aya| ee| ho|nka|i k|uuq|nim|ina|ihi|elo|waa|dan|agu|ays|a h|saa|mar|ark|ya |ank|o w|naa|gga|ee |ax | bu|uqd|qda|rri|riy|n a| no|u h|n s|oon|lka|u a|laa|o l|ab |haq|uur|int| gu|ida|iri|lad|dhi|yih|ysa|dah|to |aam|ofk| xi|arc|rci|eli|ood|ool|orr|alk|goo|ayn|e u|n x|h q|asa|sag|a c|sho|ami|i a|n q|siy| ug|kii|o u| ta| ge|gel|agg|a g| di|ido| ji|hoo|a f|al |jee|dal|ago|ii |a b|mo |iir|ooc|bar| ci|caa|xir|ra |aqo|sig| mu|aba|oob|oba|u q|aaq|aab|sad| ra|cad|dar|imo|ar |y k|fka| du|xay|y d|ras|o c|ari",
    "hms": "ang|ngd|gd |ib | na|nan|ex |id | ji|ad |eb |nl |b n|d n|ud | li|jid| le|leb| ga|ot |anl|aot|d g|l l|b l| me|ob |x n|ngs|gs |mex|nd |d d| ne|jan|ul | ni| nj|nja| gu| zh|lib|l n|ong| gh|gao|b j|b g|nb |l g| je|jex|gan|ngb| ad|end|el |gb |han| sh|ub | da|d j|t n|d l| nh|nha|b m|is |d z|x g| ya| wu|she|l j|oul|il |nex| ch|b y|d s|gho|gue|uel|wud| gi|d y|hob|nis|d b|s g| zi|lie| yo|es |it |nx |ies|aob|gia| de|eib|you|ian| hu|s j|d m| ba|zib|oud|b d|chu|ut |t j| do|ol |at |hud|nen|hen|s n|iad|ab |zha|t g|dao| go| mi|enl|x j|enb|b z|hei|eit|nt |b s| ze|d c|al |inl| xi| se| re|ren|hao|d h| fa|ngx|gx |anb|gua|yad| ho|x z|fal|b w|nib|ix |b h|and|had|t l|x m|gou|d x|bao|ant|don| xa|yan|d p|s z|hib|anx|zhe|ox |l d| pu| du|dan|gha|od |s m|sen|xin|lil|hui|uib|uan| we| di|b x|oub|t h|hub|zhi|t z| ju| ge| ng|t m|hol|xan|pud|x l| ma|jul|eud|hea|l s|enx|l z|jil|zen|aos|os |s l|d r|dei|ngt|gt | yi| he| si|nga|heb|zho|hon|did|d a| lo|b a|x c|dud|b b|lou| bi|dou|geu|b c|d k|x a|d w|wei|x b|l h|x d| qi|bad|t w| bl|blo|aod| nd|nia|deb| ja| sa|eut|ax |eab|s a| bo|lol|sat|ngl|gl | to|l m| pa|pao|b f|lia|x s|heu|t s|che| ca|can|s w|s y|sib|mis|zei|ux | pi|x r|gon|t p|jib|iel|d f| cu|ghu|unb|t c|inb| ko|x x",
    "hnj": "it | zh| ni|ab |at | sh|ang|nit|os | do|uat|ox |ax |nx |ol |ob | nd|t d|x n|nf |zhi|as | ta|tab|ef |if |d n|ad | cu| mu|cua|uax|mua|uf |b n|ib |s d|dos|id |enx|hit|nb | lo|f n|t l|ngd|gd |inf|us | go|ux |ed |she|b d|t n|b z| ho| yi|x z|aob|l n|t z|ong| zi|ix |nda|d z|yao|uab|enb|ut | de|f g| dr|dol| yo|zhe| le|euf|x d|inx| ne|nen|das|dro|ngb|gb | ge|d s|s n|f z|uef|hox|len|b g|il |ud |nd |gox| ua| na| du|x j|f y|oux|x c|han|ndo|of |f h| ja| gu| ny|zha|s z| da|uad|heu|lob|shi|ik | bu| ji|hai|ged|od |b h|t g| ya|ngf|gf | hu|ex |bua|you|rou|nil|hen|yin|zhu|out|ous|nya|is |f d|enf|b c|af |dou|lol|nad| re| ha| xa|uk |t s| id|xan|sha|hua|jai|b y|aib| qi| la|s s|d d|l m|ot |hue| xi|x g|x l|ren| kh| dl|ait| ba|aod| zo| ju|jua|zif| nz| ga| di|bao|x y|b s|x s|xin|aof| li|b b|x m|x t|eb |b l|ngx|gx |dax|b t|hef|gua| be|las|d j|s t|hed|nzh|l d|t y|hif| pi|f b|d l| ib|t h|f l|hou|dus|hun|und|s l|t r|el |uas|gai|ngt|gt |hab|aos| mo| zu| bi|f t| za|d y|x h|aik|k n|end|aid|ros| gh|zos|pin|ak |s x|d g|f s|s y| ao|k z|s b|due|mol| fu|dex|iao|x b|hik|x i|deu|l b| bo|b k|s m| lb|lb | hl|lan|uaf|d b|zho|al |eut| ro|ub |et |t c|d m|x x|d h| ch|d p|f x|t b| nt| su|uak|zis|shu|t t|gha|yua| we|oud|gon|d t",
    "ilo": "ti |iti|an |nga| ng|ga | pa| it|en | ka| ke| ma|ana| a | ti|pan|ken|agi|ang|a n|a k|aya|gan|n a|int|n t|ali|lin|a m|dag|git|a a|i p|teg|a p|nte| na|man|awa|kal|da |ng |ada|ega|nag|way|na | da|n i|sa |i k|n k|ysa|n n|al |a i|no |add|aba| me|eys|i a|nna|dda|ngg|mey| sa|ann|pag|ya |gal| ba|mai| tu|gga|ung|i s|kad|yan|tun|nak|wen| ad|aka|aan|enn|nan| ag|asa|i n|wan| we|nno|yaw|i t|l m|ata| ta|ami|a t|apa|ong| si|li |i m|kas|aki|ina|ay | an|n d|ala|a s|g k|gpa|mak|eng|ili|n p|et |ara|at |ika|ipa|dad|ama|nai|g i|yon| aw|in |ao |toy|oy |ta |on |aen|ag |bab|ket|aik|ily|lya|sin|tao|ani|agp| ki|a d|bal|oma|ngi|uma|g a|i i|kin|naa|bae|o k|y n|daa|gil|o t|iwa|ags|pad| am|syo|i b|kab|sab|ida| um|mil|aga|gim|ar |ram|yto|san|tan|min|pap|n m|eg |agt|o n|a b|aar|asi|ino|nom|nia|n w| wa| de|dey|pam|i e|sal|bag|saa|iam|eyt|day|kit|ak |ed |gsa|lak|t n|ari|nay|kan|nat|t k|i l|i u|sap| gi|g n|aw |sia|o p|o i|dum|i g|to |uka|agb|bia|aib|lub|ubo|ged| li|apu|pul|lan|imo|mon|y a|ma |pak|ias|sta|den|i d| id|bas|kai|gin|i w|kap|ita|asy|kni|kar|bon|abi|ad |umi|ban|agk|akd| ar|mid|din|sar|iba|nnu|inn|o m|ibi|ing|ran|akn|nib|isu|abs|maa|kda|aip|as | la|o a|t i|idi|nto|lal|amm|aad|or |adu|kua|ais|nal|w k|ulo|y i"
  },
  "Cyrillic": {
    "rus": " пр| и |рав|ств| на|пра|го |ени|ове|во |ани| ка|ть | по| в | об|ия |лов| св|сво|на | че|о н|ело|ост| со|чел|ие |ого|ния|ет |ест|аво|ажд|ый | им|век|ние| не|льн|име|ова|ли |ать|т п|при|каж|и п| ра|или|обо|жды| до|ых |дый|ек |воб|бод|й ч|его|ва |ся |и и|мее|еет|но |и с|аци|ии |тва|ой |лен|то | ил|ных|к и|енн|ми |тво| бы| за|ию | вс|аль|о с|ом |о п|о в|и н|ван|сто|их |ьно|нов|ног|и в|про|ако|сти|ий |и о|бра|пол|ое |дол|олж|тор| во|раз|ти |я и|я в| ос|ным|нос|жен|все|и р| ег|не |ред|тел|ель|ей |сно|оди|о и|а и|чес|общ|тве|щес| ко|ним|има|как| ли| де|шен|нно|е д|пре|осу| от|тьс|ься|вле|нны|аст|осн|а с|одн|ран|бще|лжн|быт|ыть|сов|нию| ст|сту|ват|рес|е в|оль|ном|чен|иче| ни|ак |ым |что|стр|ден|туп|ду |а о|ля |зов|ежд|нар|род|е и| то|ны |вен|м и|рин|нац|вер|оже|ую | чт|она|обр|ь в|й и| ме|аро|ото|лич|нии|бес|есп|я п|х и|о б|ем |е м| мо|дос|ьны|тоя|еоб|ая | вы| ре|и к|кот|ное|под| та|жно|ста| го|гос|суд|ам |ава|я н| к |ав |авн|ход|льс|нст| бе|ово|и д|ели| дл|для|ной|вов|ами|ате|оро|дно|ен |печ|ече|ка |еск|ве |уще|в к|нен|мож|уда|о д|ю и|ции|ког|вно|оду|жде|и б|тра|сре|дст|от |ьст|е п|нал|пос|о о|вны|сем|азо|тер|соц|оци|циа|ь п|олн|так|кон|ите|обе|изн| др|дру|дов|е о| эт|х п|ни |еди|дин|му ",
    "ukr": "на | пр| і |пра|рав| на|ння|ня | за|ого| по|го |ти |люд| лю|во | ко| ма|юди|льн|их |аво|о н| не|анн|дин| св|сво|кож|ожн|пов|енн|жна| до|ати|ина|ає |а л|ува|не | бу|обо|аці|має| як| ос| ви|є п| та|аль|або|них|ні |ть | ві|ови| аб|бо |а м|ере|і п|без|вин|при|о п|ног|іль|ми |ом |та |ою | бе|ста|воб|бод|до |ост|ті | в | об|ва |о в| що|ий |ся | сп|і с|від|нов|кон|и п|ств|инн|нан|ван| у |дно|она|ват|езп|пер|но |ій | де|ії |ідн|и і|сті|під|ист|нні|ако|ьно| мо|бут|ути|ід |род|і в|що |ава|тис|а з|вно|ну |и с|ої |і д|ду |а в|ів |аро| пе|ний|а п|му |соб|яко|спр|і н| рі|рів|чи |ним|ля |нар|лен| ін|у в|нен|ому|нац|ися|и д|ова|ав |і р| ст|ові|нос| пі|ють|сть|ово|про|одн|у п|віл|овн|вни| ро| її|її | вс|ном|і з| ра| су|мож|чен|ві |буд|іст|івн|оду|а о|ни |сно|ими|а с| со|ьни|роз|и з|зна|я і|о д|х п|е п|о с|и в|дер|ерж|им |чин|рац|ції|і б| од|а н|сі |сту|тер|ніх|ди |їх |нна|так|о з|я н|заб|зпе|у с|спі| ні|е б|ржа|осо|я п|в і|кла|то |а б|осн|рим|сві|віт| дл|для|тва|ами|зах|рес| ре|ков|тор|соц|оці|ціа|і м|ки |тан|абе|печ|ког|ага|гал|ту |ї о|е м|оже|же |удь|ніс|ара|руч|авн|и щ|ною|я в|всі|кої|ини|ь п|осв|і і|ахи|хис|іал|а і|оди|тво|жен|нь |нал|ваг|аги|ги |інш|лив|х в|заг|роб|піл|в я|ком|об |о у|жав|і о",
    "bos": " пр| и |рав| на|пра|на |да |ма |има| св|а с|а п| да| по|а и|је |во |ко |ва | у |ако|о и|но | за|е с| им|аво|ти |сва|ава|о н|и п|ли |или|и с|вак|ост| ко|их |не |а у| сл|вањ| не| др|ње |кој|ије|ња |и д| би|ств|им |у с|јед| ил|сло|лоб|обо|бод| је| ра|при|ање|вје| об|а д|ом |се | су|е и|ју | се|сти|и и|а б|дру| ос|е п|вој|циј|у п|о д|а о|раз|су |ања|а н|ује|ова|у и| од|и у|ло |едн|ни |у о|ово|аци|ити|о п|а к|оје|жав|нос|дје|е о|бра|пре|шти|а ј|про|и о| ка|них|бит| тр|тре| бу|буд|у з|ог |ста|ја |држ|ржа|е д|миј|сво|реб|авн|ија|и н|е б|ђи |пос|ту |аро|род|ред| ње|ба |а з|ка |де |ем |ају|ива|ве |е у|јер|бил|ило| из|ени|ду | до|а т|за |еђу|нар|тва|одн|њег|гов| са|ним|м и|вим| ни|у д|јел|о к|оји| см|дна|уђи| ст|алн|ист|и м|еба|ран|ичн|вно| дј|у н|ода|нак|е к|ан |нов|сно|сту|нст|ено|чно|ани|ном|е н|тив|нац|аве|и б|сми|чов|овј|осн|а р|нап|ови|анс|дно|оја|ног|м с|еди|ара|ој |ну |кри| кр|оду|ико|рад|ник|туп| чо|јек|тво| вј| ми|тељ|обр|жив|заш|ашт|тит|уна|его|под|сам|о о|руг|ји | мо|ву | ов|х п|уду|рив|ење|дст|те | те|а ч|вни|сви|и в|ина|и т|ра |ите|у у|иту|међ|ак |дни|ниц|њу |нич|одр|вољ|ави|г п| оп| та|рим|кла|е т|ао | вр|акв|тно|мје|дуђ|она|ада|сто|оби|едс|то |оди|о с|ку |риј|у м|од |ичк|вен",
    "srp": " пр| и |рав|пра| на|на |ма | по| св|да |има|а п|а и|во |ко |ва |ти |и п|ако| у |а с| да|аво|ост|и с| за|о и|сва| им|вак|је |е с|ава| сл|о н| ко|ња |ом |но | не|не |ли |у с| др|или|сло|обо|кој|их |лоб|бод|им |а н|сти|ств|а о|ју | ил| би|при|а у| ра| је|ог |јед|ње |е п|у п|ни |а д|и у|едн|ити|нос|а к|о д|ање|ова|про| су|и и| ос|вањ|ста|дру|е и|циј|се |род| об|и о|ања|ају|е о|ове| де|аци| ка|ово|ја | ни| од|ве | се|и д| ст|м и|авн|и н|ује|ени|ија|дна|жав|у и|ред|су |нов|оди|вој|тва|е б|оја|што|у о|ара|а б|држ|ржа|одн|ним|ран|пош|ошт|а ј|ка | ов|у у|ду |аве|осн|сно|шти|аро|раз|бит|а з|у з|ења|де | из|них|о п|у д|е д|пре|ву | тр|ту |еђу|нар|гов|без|ичн|за |вно|ло |у н|оји| са|то |ан |нак| ме| ње|чно|сво|вим|нац|ода|ји |ани|ме |ико|ник|ика|е к|пос| кр|тре|реб|нст|сту|е у|ку | до|ашт|тит|алн|дно|њег|ном|ног|м с| вр|о к|ој |чов|анс|ови|о с|бра|те |тав|туп|ено|жив|заш|ем |и м|дни|вар|рив|руг|вољ|ави|штв|е н|као|ао | ли|ст |ило|њу |сме|о ј| см| чо|оду|вре|дст|од |а т|кри| бу|буд|и в| ве|вер|а р|дел|тво|међ|оје|м п|ављ|его|под|ена|уги|ла |пор| оп|руш|ушт|ком|еди|ба |кла| мо|и б|как|акв|рад|ну |век|рим|гла|јућ|уде|вни|еба|е р|сви|м н|иту|тер|ист|а ч|пот|рем|ниц|у в|х п|ива|сам|о о| он|езб|збе",
    "uzn": "лар|ан |га |ар | ва| би|да |ва |ир | ҳу|ига|уқу|бир|ҳуқ|қуқ|ган| ҳа|ини|нг |р б|иш |ни | та|лик|инг|ида|а э|или|лиш|ари|нин|иши| ин|инс|он |ади|нсо|сон|ий |лан| ма|дир|кин|и б|ши | бў|ҳар|бўл| му|уқи|дан|ила|қиг|р и|қла| эг|эга| ўз|а б| эр|эрк|кла|қил|оли|ки |гад| ол|нли|лга|и ҳ|рки|лиг|иги| қа| ёк|ёки|иб |н м| ба| қи|н б|ри |ара|атл| бо|ҳам|лат|бил|ин |р ҳ|а т|ала|лаш|бош|ик |инл| эт|ш ҳ|а ҳ|и в|ниш|тла|эти|тил|мас|а қ|и м|оси|им |ат |уқл|арн|рни|қар|ани|а и|ўли|ги | да|н ҳ|риш|мла|ли |и т| ха|арч|рча|ча |бар|аси|ўз |а а|рла| бу|а м|лин|ати|ият|либ|таъ| ту| ас|тиб|аро|а о|ун |тга|р в|икл| бе|мум|н қ|али| те|сид|ш в|мат|амд|мда| ке|лла|шга|н э|дам|амл|хал|ола| қо|ири|ирл|илл|а ш|рга|иро| шу|тиш| де|умк|мки| эъ|тен|енг|а ў|рда| са|гин|имо|тар|а ё|ур |рак|алқ| ки|аза|к б|ода|сий|а к|тни|ўлг|н в|нда|шқа|зар|н о|и қ| ми|мил|р т| си|ак | ша|ор |кат|ера|сиз|ам |асо|сос|н ў|шла|н т|нид|ошқ|й т|ана|ти |рин|асл|бу |син|дав|илг| со|ас |нга|лак|ино|ъти|муҳ|а в|аш |бор|лим|уни|лли|н и|си |и э| ка| то|а д| жа|ами|қон|на |риг|лма|кер|авл|вла|и а|дек|екл|ят |ака| эм|эма|эът| ҳе|ҳеч|еч |ким|икд|кда|сит|лад|и ў| ни|ник|ага|и о|и с| уч|учу|чун|аъл|ъли|анл|аёт| иш|а у|ўзи|диг|ай |ада|оий|мия|тда|а с",
    "azj": " вә|вә |әр |лар| һә|ин |ир | ол| һү|һүг|үгу| би|гуг|на |ләр|дә |һәр| шә|бир|ан |лик| тә|р б|мал|лма|асы|ини|р һ|ән |шәх|әхс|гла|ары|а м|дир|али|аг |угу| ма|илә|ын |уна|јәт| ја|ара|икд|ар |әри|әси|р ш|рин|әти|дән|нин|н һ| аз|јјә|син| мү|тин|ни |зад|ијј| мә|әрә|мәк|үн |нда|әт |и в|ыны|нын|ндә|ә а|аза|рын|гун|олу| ет| га|лыг|илм|кди|лә |ә ј|әк |лмә|ына|инд|олм| ин|хс |мас|сын|лун|ә б|да |н т|әми| бә|адл|длы|н м|нә |г в|ја |тмә|бәр| он|ә һ|әја|сы |нун|етм|дан|инә|маг|ун |раг|ә в| ва|н а|н б|рлә|ә м|си |әра|н и|ынд| өз|аны|нма|инс|ама|ры |ығы|ли |ил | ал|ала| ди|ә д|ик |ирл|ләт|а б|бил|ыг |мүд| сә|ә и|ны |нла|әлә|дил|алы|ә е|унм|н в|ола|аси|үда|или| дә|нса|сан|угл|ә о|хси| һе|уг |әтл|һәм|әни|ејн|у в| да|тәр|мин|јин|кил|дыр| бу|лан|иш | һа|мәс| ки|ми |лығ|ә г|г һ|и с|даф|афи|фиә|р в| иш| әс|сос|оси|сиа|хал|алг| та| ас| ед|бу |һеч|еч |рә |јан|ы һ|ким|ији|ы о|ина|сиј|әса|сас|а г|јар|лы |түн| ис|ист| со|ал |н ә|ифа|әмә|ә т|мил|илл|лыд|ыды|ыр |әдә|ыгл|лиј|тлә|а һ|мәз| бү|бүт|үтү|ија|иә | үч|үчү|чүн|т в|дах|ахи|хил|р а|ылы|ман|сил| се|сеч|адә|иал|ону|өз | ҹә|миј|әји|н е|еди| ми| нә|а в|мән|рил|әз |илд|раб|абә|шәр|ғын|аја|зам| ни|улм| ха|тәһ|әһс|һси|и һ|сти|гу |вар|ад |там|уғу|з һ|ган|рәф|н д",
    "koi": "ны |ӧн | бы|да | пр|пра|лӧн| мо|рав| да|быд|лӧ |орт|мор|ӧм |аво|ӧй | ве|нӧй|ыд |ыс | не|сӧ |ын |тӧм|во |сь |эз |льн|ьнӧ|тны| ас|д м|ыны|м п| и |сьӧ| по| ӧт|то |бы | эм| кы|тлӧ|эм | от|аль|н э|вер|ртл| кӧ|ӧ в| ко|ерм|ств|воэ| до|тшӧ|ола|ылӧ|вол|ӧс |ы с|ліс|ісь|а с|ас |кыт|тво|кӧр| се|нет|ето|шӧм|ӧдн| ме|мед| ол|злӧ| вы|ӧ д|ӧ к|та |аци|ӧ б|вны|лас| на|з в|ӧрт| во|на |а в|ась|ыдӧ| сы|едб|дбы| вӧ|лан|рмӧ| оз|оз | сі|ытш|оэз|ӧтл|ы а|оти|тир|с о|олӧ| чу|ись| эт|ция|рты|тыс|ы б|кол|ы п| го|сет|кӧт|тӧг|ост|тӧн|н б| со| сь|рті|ӧтн|н н|дз | ке|кер|о с|мӧ |ӧ м| мы|ис |а д|ӧг |дӧс|ест|нӧ |пон|онд|ы н|сис|нац|итӧ|н п|суд| уд|удж|выл| ви|эта|н м| эз|ана|ӧны|с с|ть |орй|йын|сси|рре|рез|ьӧр|ті |сыл|ысл|нда|мӧд|з к|а п|с д|ӧр |чук|укӧ|рны|ӧмӧ|кин|рт |овн|ӧт |она|нал| об|ӧ о|отс|лӧт|й о| тӧ|тӧд|дны|асс|кон|слӧ|ы д|скӧ|с в|с м|ытӧ|езл| ло|быт|осу|эзл|кӧд| ум|умӧ|мӧл|ӧ п|асл|тра| ст|ь м|сьн|ьны|ь к| ов|код|сть|а н|ы к|тла|а к|ӧтч|дор|иал|а о| пы|н к|оль| за|аса| дз|нек|а м|н о|етӧ|ӧ н|ерн| сэ|ы м| де| чт|что|йӧ |ы ч|еки|поз|озь|езӧ|вес|ськ|исӧ|ӧтк|тко|рйӧ|ион|ннё|з д|ӧмы|тсӧ|са |кыд|енн|соц|оци|циа|й д|пыр|зын|нӧя|ӧя |зак|ако| мӧ| а |еск|а б|ан |тӧ |гос|уда|дар|арс|рст|рлӧ|ӧ с| ли|эсӧ|оля|мӧс|ӧсь|дек",
    "bel": " і | пр|пра|ава| на|на | па|рав|ць |ны |або|ва | аб|ацы|ае |аве| ча|анн|льн|ння| ма| св|ала|сва|не |чал|лав|ня |ых |ай |га | як|век|е п| ад|а н| не| ко|ага|пры|кож|ожн|а п| за|жны|ы ч|дна|бод|а а|цца|ца |ваб| ў |мае|ек |і п|ных|нне|пав|а с|асц|бо |ам |ста| са|ьна|ван| вы|одн|го |аць|наг|він| да|дзе|ара|мі |цыя|оўн|тва| ра|і а|то |ад |ств|аві|лен| ас|і с|най|аль|енн|і н|ці |аро|аво|рац|сці|пад|к м| яг|яго|іх |ама| бы|рым|род|і і|ым |энн|што| та|я а|нан|ана|нас| дз|ні | гэ|гэт|а ў|інн|а б|ыць|чын|да |оў | шт|ыі |а і|агу|які|ным|дзя|я п|цыі|і д|ьны|нар| у |ўна|оль| ўс|х п|нац|ыя |ах | ус|ымі|ля |амі|ыма| ні| гр|воў|ў і|адз|эта|іна|ход|о п| ка| ін|ы п|зна|нен|аба|быц|рад|ўле|чэн|ь с|чы |сам| ст|асн|і р|ду |аў |ера|ры |нал|жна|уль|рам| су|аны|кла|аюч|ючы|оду|ую |а р|ўны|маю|ука|кац| дл|для|ь у|пер|е і|нае|ако|і з|гра|адс|ыцц|яўл|і ў|яко|а з|кан| ро|роў|нст| шл|адн|ода|аса|аду|нав|вы |ы і| пе|і м|кі |але|х і|авя|алі|раб|мад|дст|жыц|раз|зе |нна|ані|х н|е м|ада|нні|ы ў|о н|дзі|я я|люб|аюц|бар|дук|ахо|а в|сац|авы|так|я ў|тан|зак|чна|заб|бес|я і|ваг|гул|ім |ган|зяр|ярж|ржа|жав|ве |е а|м п|ацц|од |ены| дэ|ну |у ш|нах|вол|а т|ога|о с| бе|інш|ака|усе|яна|ека|ка |сно|рас| рэ|ь п|ніч|чац|се |і к",
    "bul": " на|на | пр|то | и |рав|да | да|пра|а с|ва |ств|та |а п|ите|но |во |ени|а н|е н| за|о и|ото|ван| вс|не |ки |те | не|ове|о н| по|а и|ава|чов|ия |ане|ни | чо|ие |аво| св| об|а д|е п|век|ест|сво| им|има|и ч|ани|ост|и д|ние|все|тво|или|ли |и с|вот|а в|ма | ра|ват|и п|сек|еки|ек |а о|и н| в |е и| ил|ова|при| се|ето|ата|аци|воб|обо|бод|к и|пре|ат |оди|раз| съ| ос|а б| бъ|ред| ка| ко|лно|ния|о д|бъд|о п|се | от|за |о в|ъде|ята| е | тр|и и|о с|тел|и в|от |ран|е с|нит| де|ка |бра|ен |общ|де |алн|и о|ява|ият|ция|про| до|нег|его|а т|нов|ден|как|ато|ст | из|а ч|тря|ряб|ябв|бва|а р|а к|вен|о о|щес|а з|ено|гов|тве|нац|дър|ърж|ржа|е д|нос|лен|ежд|род|е о|и з|вит| та|зи |акв|ез |она|обр|нар|нот|иче|о т| ни|кат|т с| с |йст|авн| бе|осн|сно|вни|пол|рес|аро|кой|зак|е в|тва|нен|е т|ува| ли|ейс|жав|едв|стр| ст|без|вси|сич|ичк|чки|вид|си |жен|под|еоб|нст| те|ди |ри |сто|ган| дъ|а е|и к| че|ода| ср|сре|ака|чес|и р|и м|т н|одн|о р|лич|елн| ре|бще|ник|ели|че |дви|еме|ира|жда|кри|лни| си|са | то|ой | ме|оет|гра|ход|дру|ичн|еди|дос|ста|дей|я н| къ|ан |ико|чре|й н|ави|нал|пос|тъп|ра |азо|зов|рез|той| со|меж|тно|т в|и у|нет|нич|кон|клю|люч|нео|чит|ита|а у|а м|дно|оят|елс|лит|ине|таз|ази| мо|що |т и|изв|тви|чен",
    "kaz": "ен |не | құ|тар|ұқы| ба| қа|ға |ада|дам|құқ|ық | ад| бо|ына|ықт|қта| жә|ар |ың |ылы|жән|әне|мен| не|лық|на |р а|де |ін |ары|а қ| жа|ан | әр|қыл|ала|ара| ме|уға|н қ|еме|ның| де|іне|ам |асы|тан|лы |әр |да |ста|нды|еке|ығы| өз|ған|анд|ын | бі|мес| қо|бол|бас|ің |ды |етт|ып |ілі|н б|нде|ері|е қ|қық|бір|лар|алы|нем|есе|се |а ж|ы б| ке|тын| ар|е б|бар|ге |ост| ти|тиі|олы|ік |інд| та|аты|сы |е а|дық| бе|ы т|нда| те|ры |ғы |бос|ғын|луы|иіс|сын|рде|рын|еті|қығ|алу|іс |рін|іні|е ж|дар|ім |егі|н к|қар| ер|тті|н ж|ыры|аны|лға| са|уын|ынд|ыны|ы м|рға|ген|ей |тік|тер|нің|ана|уы |аза| от|нан| на|е н|гіз|тық|мыс|ы ә|мны|м б|өзі|сқа|қа |е т|ң қ|еге|ке |ард|нег|луғ|лан|амн|кін|і б|асқ|рал|ті |ру |айд|тта| же|а б|р м|рды|кет|аса|ді |өз |ама|дей|н н|тыр|ауд|ігі|лып| мү| ал|зін|лік|дай|мет|жас|бер|тең|арқ|рқы|а о|е ө|қам|елі|рлы|ы а|а т|дер|біл|р б|еле|қор|ден|тін|уда| тү| жү|кел|і т|ір |лге|ы ж|ең |а д|тты|оны|гін| ха|ркі|лде|е м|н т|түр|оға|ікт|кті|зде|жағ|уге|ауы|рыл|ұлт|лтт| ос|осы| то|ция|ы е|н а|ау | ау|ені| ел|н е|оты|шін|ыс |қты|імд| да|сіз|лма|кім|ң б|лім|қат|зі |орғ| әл|хал|ерк|ек |құр|тте|е д|ағд|ғда|елг| ас|ірі| ұл|ағы|амд|тал| со|рып|ылм|лін|ным|мас|сыз|дан|із |ірд|ай |гі |сты|ым‐|ң ж|с б"
  },
  "Arabic": {
    "arb": " ال|ية | في|في |الح| وا|وال| أو|ة ا|أو |الم|الت|لحق|حق |كل |لى |ان |ة و|الأ| لك|لكل|ن ا|ها |ق ف|ات |مة |اء |ما |و ا|ون |ته |الع|أن |ي ا|ي أ|شخص|ة ل|الإ| عل| أن|م ا|حري|الا|من |على|حقو|قوق|ت ا| شخ|لا |ق ا| لل|فرد|رد | أي|أي |رية| كا|د ا| ول| من| إل|خص |ا ا|وق |نسا|ل ف|ا ي|ه ا|ة أ|كان|ن ي|امة|جتم| حق|الق|ام |دة | لا|ل ش|إنس|سان|ين |ة م|اية|ن ت|ا ل|ذا | فر|ن أ|هذا|لة |اً | عن|ى ا|لتع|اسي| دو| حر|ع ا|ه ل|لك |ه و|ترا|له |ماع|د أ|ي ح|إلى|الج|الد|، و| با|ن و|ي ت|نون|لعا|مع | هذ|ة ع|لحر|يات|عية|ص ا| وي|لإن|لأس|أسا|ساس|سية|بال|ي و|حما|ماي| إن|الف|انو|ير |رام|ا و|عام|دول|مل |الو| مت| له|الب|ساو|ة ب|هم |ع ب|علي|ك ا|لقا|قان|تما|ة ت|ى أ|ول |ة ف|ا ب|اد |الر|ل و|ل ا|انت| قد|لجم|لمي|لتم|تمت|اعي|ليه|لمج|ه أ|ا ك|ال |لأم|لمت|لإع| يج|لدو|ق و|ريا|يه |رة |ن ل|دون|تمي|كرا|يد |ذلك| يع|ر ا|تعل|عال|تسا|لاد|اة |قدم|متع|تع |اجت| كل|مان|غير|اته|م و|مجت|تمع| مع|مم |لان|يجو|جوز|وز |عمل|دم |فيه|الض|ميي|ييز|متس| عا|أسر|ن م|معي|لات| مس|لاج|عن |ي إ|ليم|يم | أس| تع|يز |مية|جمي|ميع|الش|اعا|ة، |الس|شتر|لمس|لما|ني |لي |يها|ملا|ود |تي |لضم|ضما|اعت|ر و|اق |ي م|ي ي| بح| تم|تنا|أمم|تحد|حدة|إعل|علا|ه ع| جم|عة |م ب|ولم|الن|ل إ| به|ب ا|اوي|قد |أية|قيد|د ب|اك |وية|إلي|لزو|د م|مست|كاف|وله|ه ف| ذل| وس|لحم|نت | أم| مر|مرا| وأ| وع",
    "urd": "ور | او|اور|کے | کے| کی|یں | کا|کی | حق|ے ک|کا | کو|یا |نے | اس|سے |ئے |کو | ہے|میں| می|ے ا| کر| ان|وں | ہو|اس |ر ا| شخ|شخص|ی ا| جا| سے|حق |ہر |خص |ے م|ام | یا|ں ک|ہیں|سی | آز|آزا|زاد|ادی|ائے|ہ ا|ص ک|ا ح|جائ|ہے |کہ |ر ش|ت ک| پر|ی ک|م ک|۔ہر|پر |ا ج|ان |دی |س ک|ق ہ|ہے۔|ر م|ں ا|ی ح|و ا|ار |ری |ن ک|کسی|حقو|قوق| مع|ے گ|ی ج|وق | ہی|ر ک|سان|نی |کرن| حا| نہ|تی |ی ت| جو|ئی |انس|نسا| کہ|اپن|ل ک|جو | اپ|ے ب|یت |نہ |ہ ک| مل|ہو |می |ل ہ|رے |ی ش|رنے|ے ل|ے ہ| کس| ای|ا ا|۔ ا|حاص|اصل|صل |معا|نہی|ی م|وہ |یں۔| تع|انہ|ق ک|د ک|ی ب|ات |ملک|ایس|ی ہ| بن| قو|قوم|کیا|ے، |عاش|اشر|ر ہ| گا| دو|یہ |وام|دہ |ں م|ا م| من|بر |انی|ے۔ہ|ر ب|دار|ے ج| وہ| لئ|لئے| عا|اقو|قوا|مل |ائی|علا|اد |ی س| جس|ر ن|ے ح|ہ م|کر |و ت|لیم| و | قا|انو|ا ہ|جس |یوں| یہ|لک |ریق|ے۔ |نیا|تعل| گی|گی |ر پ|دوس|ی آ|یم |، ا| اق|وئی|یر |پنے|ے پ|م ا|گا۔|یاد| رک|علی| مس|ی، |ین |ن ا|انے|وری|ی ن|لاق|ر ع|ون |خلا| با|ا س| سک| دی| چا|رائ|ومی|ہ و|نا |اری| بر|رکھ|ندگ|دگی|ر س|رتی| بی| شا|س م|ق ح|ادا| مم| ہر|ا پ|و گ|وسر|سب | پو|قان|نون| بھ|ے خ|اف | اع| مر|یسے| پی|غیر|ے س|ال |ت ا|، م| مح|ں، |بنی| ذر|ذری|ریع|ہوں| عل|تما|مام|ونک|نکہ|دان|پنی|ر ح| ام|من |عام|پور| طر|ے ع|ائد|بھی|ھی | مت| مق|د ا| خل|لاف|اعل|کوئ| لی|و ک|ے ی|ا ک|ر آ|دیو|اں |چون|، چ|یاس|برا|کرے|ی ع|ر ج",
    "fas": " و | حق| با|که | که|ند | در|در |رد | دا|دار|از |هر | از|یت | هر|ر ک|حق |د ه|ای |ان |د و| را|ود |ین |یا |ارد|کس |ی و|را | یا| کس| بر|باش|د ک|ه ب| آز|آزا| خو|ه ا| اس|د ب|زاد|ار | آن|ق د|شد |حقو|قوق|ی ب|ه د|ده |وق |ید |ی ک|ر م|خود|ور |و ا|رای|اشد|ام |تما| اج|ری |ادی|س ح|دی |اید|است| ان|نه |و م|د ا|ر ا| بی|با |انه|ی ا|د، |ون | تا| هم| نم|ات |مای|ا ب|ایت|ر ب| بش| کن|انو|اسا| مر|ست | مو| مل|برا|وان|این|جتم| می|ورد| شو| ای|ن ا| اع| به|ت و|، ا|اجت|ماع|عی |ا م|ائی|ئی |و ب|نی |ملل|ت ا|و آ|آن |بشر| زن|ی، |کند|ن م|ن و|بای|شود|ی ی|های| من|شور| مس|کار|ت ب| بد|دان|اری|اعی|د آ|مل |ز آ|یگر|ی ر|ت م|مور| گر|گرد| مق|توا|ی م|علا|یه |ن ب|میت| شد| کش|کشو|ه و|ق م|د ش| مج| اح|ن ت|و د| حم|لی | کا|ت ک|هٔ |نون|مین|دیگ| عم|انی|ر خ|ه م| مت|ن ح|ی د|لام|رند|اه |نجا|بعی|نوا|ساس|ساو|د م| آم|ادا|وی |گی |هد |ا ک|اد |ی ح| مح| قا|قان|می |یده|مقا|لل |ر ش|ق و|اعل|ا د|شده|ع ا| بع|اسی|د ت|همه|سان|شر | عق|ر و|دگی|حما|ا ه|خوا|‌ها|ه‌ا| او|او |اده|اً |ر ت| دی|ومی| شر|نمی|بر | هی|هیچ|یر |ز ح|مند|بین|تی |جا |عقی|یتو|م و|مسا|و ت|سی |اوی|بهر|م م|ر د|انت|زش |ارن|زند|ندگ|و ه|رفت|رار|واه|ا ر| بو|تأ|أم|ٔمی|ران|عمو|موم|ی ن|اند|ل م|ردد|ه ح|عیت| فر| بم|دیه|ا ا|نما|آنج|کلی|احد|حدی|مال| تع|و ح|مرد|ت، |ملی|ق ا|واد|م ا|د د| خا| ار|اشن|شند",
    "zlm": " دا|ان |دان| بر| او|ن س|له | ڤر|كن |ن ك|دال|ن ا|ن د|رڠ |حق |يڠ | كڤ|ارا| يڠ|أن |تيا|ڤد |ورڠ|ڠن |اله|ياڤ| تر|ولي|ن ڤ|اور|كڤد|برح|رحق|ين |ستي|اڤ |را |ليه| ات|ه ب| ست|يه |اتا| عد|عدا|ن ب|تاو|ڤ ا|او |ن ت|بيب|يبس|سي | كب|ه د|ن م| من| سو| سا| حق|ق ك|اسا|سام| تي|ن ي|الم|لم | اي|ن، |رن |اتو| ما|د س| با|باڬ|نڬا|ڬار| مم|كبي|بسن|سن |اين|ڠ ع|ڽ س|چار| سب|ي د|ندق|د ڤ|اڽ |اڬي|سبا| ڤم| د |نسي|ا ڤ|هن |قله|يند|تا |ي ا|ام | بو|ڬي | نڬ|اون|تن |وان|ا س|مأن|أنس|ڠ ب| كس| سم| سچ|سچا|ا ب|بول| مأ|سيا|ساس|اسي| ڤڠ|بڠس| دڠ|دڠن| ڤو|ڤا |ت د|رتا| هن|هند|دقل|ي م| اس|ادي|نڽ |ات |تره|رها|هاد|ادڤ| لا|تي |ڤرل|مان|، ك|بار|ارڠ|ق م|ڤون|ون |، د|اي |اول|ق٢ |٢ د|ڠسا|تو |يكن|وين|ن ه|اكن|يأن|وڠن|دڤ |وا |ا د|ن٢ |نتو|وق | سس|ماس|اس |ه م|مرا|ندو| ان| بي| مڠ|ڠ٢ |ائن|رات|يك |حق٢|برس|اد |ي س| كو|مڤو|ري | مل|وات|واس|ڤمب|، ت| سر|سرت|امر|سمو|اڬا|رلي|لين|دوڠ|ل د|تار|ڠ م|، س|وند|ي ك|لوا|سوا|ارك|تيك|ڤري|رسا|ياد|ريك|ا، |ونت|ڠ ت|ترم|ڤرا|سأن|اڤا|ي ڤ|ا ا| در|رأن| ڤن|سوس|ورو|ڠ س|لائ| بڠ|٢ ب|توق|دير|يري|وكن|جوا|هار|ندي|ارأ|وه |كرج|ڠڬو|ي، |موا| كأ|اجر|جرن| به|بها| مر|راس| كم|و ك|نن |ڤرك|ندڠ|دڠ٢|ا م| سڤ|ا٢ |سات|ق ا|ڤ٢ |شته|تها|سال|ينڠ|سسي|وهن|مول|منو|وبو| دل|وار|كور|د ك|ا ك| ڤل|لاج|ڠ ا|مبي|نتي|تيڠ|وسي|يال|ال |انت|نتا|بس |هڽ |ن ح|ه ا|كڤر|ڠ د|م س",
    "skr": "تے |اں | تے|دے |دی |وں | دا| کو| حق|کوں|ے ا| دے|دا | دی|یاں| کی|ے ۔|یں |ہر | ۔ | ہے|ہے | وچ|کیت| ان|وچ | شخ|شخص|ال |ادی| حا|اصل| نا|ے م|خص |ں د|حق |حاص|صل |یا | ای|ل ہ|اتے|ق ح|ے و| ات|ں ک|سی |ہیں| مل|ی ت|نال| از|ازا|زاد| او|حقو|قوق|ار |ا ح| ۔ہ|۔ہر|ر ش|ے ج|ص ک|وق |دیا|نہ |یند|ندے| یا| کر|ئے | جو|کہی|ی د|انس|نسا|سان|وند|ی ا|یتے|و ڄ|ڻ د|یسی| وی|ا ا|ملک|ے ح|ے ک| ہو|ے ب|ں ا|ا و|ئی |ر ک|تی |آپڻ|وڻ |ندی| نہ|ویس| آپ| جی|اون| کن|انہ|ن ۔|جو |ی ن|ان | کہ|ری | تھ|ے ہ| ڄئ|۔ ا|ے ن|ی ۔|ڻے | ہر|ام |دہ |ں ت|ں و|ں م|تھی| من|کو |ی ح|کنو|نوں|ہاں| بچ|ے ت|رے |ون |ی ک|ور |ہکو|نی |یاد|ت د|یتا|ی ہ|نہا|ن د|اری|تا |لے |ڄئے|ے د| ہک| قو|پڻے|می |ی م|قوا|وام| ون|ق ت|اف |ل ک|اے | تع|ین |چ ا|خلا|ل ت|لک |ہو |ارے| و |انی|جیا|ے س| سا|ن ا|دار|یت |ی ج|ئو |ی و| اق|علا|کرڻ|ونڄ|ات | اے|ر ت|ق د|الا|ہوو| چا| رک|بچئ|چئو|وری| وس| لو| پو|پور|قنو|نون|ہ د|ے خ|ایہ|و ا|این| ڋو| خل|لاف|ڻ ک| جا| ۔ک| عز|عزت|ا ک| مع|ے ع|یر |قوم|ں آ|او |اد |ب د|ریا|مل |رکھ|وسی|سیب|یب |کاں| قن|اقو|رڻ |وئی|ں ج|ا ت|ل ا|زت |ت ت|ر ا| سی|لا |وے |ہی |ا م|ے ر|تعل|ں س| سم|یوڻ|ر ہ|ڻی |اوڻ|لوک|م م| مت|متح|تحد|حدہ|ایں| اع|ے ذ| جھ|جھی|کوئ|کار| کھ|ہ ا|ھین|م ت| کم| ہن|ہن |ی، |ں ب|د ا|سار|ن ک|علی|لیم|نڄے|ڄے |ی س|یہو|ھیو|ائد|و ک|ائی|ے ق| مط| سڱ|سڱد| ذر|ذری|ھیا|نے |کیو",
    "pbu": " د | او|او |په | په| چې|چې | حق|ي ا|ره |ې د|نه |و ا|و د|ه ا|هر |ه و| څو|ه د|ري |حق |ي چ| کښ|څوک|وک |وي | شي|له |غه |کښې|ښې | سر| لر|لري|و پ|ه پ|ټول|لو |يت |سره|کړي|ي۔ه|ه ک|ي، |ر څ| ټو|ق ل| له|يا | هغ| از|۔هر|د م|ازا| کړ|دي |هغه| کو|نو |د ا|حقو|قوق|زاد|ه ت| پر| وا|ولو|خه |ه ه| وي| څخ|يو |ه م| يا|ول |د د|څخه| دي|ه ش|کول|ي د|ته |ه ب|ګه |و ي|ړي |اد |و م|ونو|شي۔|د ه|دې |خپل|واد| مل| هي| نه| تر| تو|د پ|ک ح|ې پ|ان |ولن|ني |ه ح|يوا|تون| با|ادي| هر| يو| مس|ي و|ې ا|لي |ې و|ي پ|د ت|يد |امي|وقو|شي |ړي۔|دان|انه|وګه| عم|هيو| دا| دغ|قو |ي۔ |ه چ|ار | خپ|بشر|توګ|اند|هغو|لني|باي|ايد| ده|ه ن|وي،|و ه|، د|ي ح| بر|غو | تا|ين |ايت| شو|شوي|دغه|مي |م د|دهغ| من|و ح| لا| ډو|ډول|بعي|پل | بش| ته|اوي|ه ګ|د ب|نيز|پر |ده |و ت|انو|نون|ون |ومي|رو |هيڅ|يڅ |ي ت|علا|ه ي|ه ل|وم |کار|ساو|تر |وند|ونه|يه |ن ک|مين|موم|و ک|اتو| اع|اعل|لام|اره| ځا|مسا| ان|د ټ|ټه | ګټ|ي ش| بي| مح|قان| پي|و ر|اخل|تو |اسي|سي | وک|ديو|ځاي|عقي| ور|لان|ل ت|ه س|ې چ| وس|و س|وون| ژو|ژون|يز |وکړ|کي |ن ش|ندې|ک د| اس| قا| نو|عمو|لتو|و ب|پار|ولے|لے |ې ک| عق|۔هي|څ څ| را|بل | بل|وسي|ت ا|ر د| ار| هم|هم |دو |ي م|مان|اسا|رته|شري|ا د|ر م|ښوو| رو|ګټه| غو|ونک| وړ|مل | شخ|شخص| اج|د ق|تام|وق |ملت|و ن|من |و څ|ا ب|ن ا|قيد| چا|ل ه| تب|تبع|ر پ|حما| کا|د خ|ر س|اني|نځ "
  },
  "Devanagari": {
    "hin": "के |प्र| के| और|और |ों | का|कार| प्|का | को|ं क|या |ति |ार |को | है|िका|ने |है |्रत| अध|धिक|की |अधि|ा क| कि| की| सम|ें |व्य|्ति|क्त|से | व्|्यक|ा अ|में|मान|ि क| स्| मे|सी |न्त|े क| हो|ता |यक्|ै ।|क्ष|त्य|िक | कर| या|्य |भी | वि|रत्|ी स| जा|र स|्ये|येक|ेक |रों|स्व|िया|ा ज|त्र|क व|र ह| अन|्रा|ित |किस|ा स|िसी|ा ह| से|ना |र क| पर| सा|गा |देश| । | अप|ान |समा|्त |े स|्त्|ी क|ा प| ।प|वार| रा|न क|षा |अन्|।प्|था |ष्ट| मा|्षा|्वा|ारो|तन्| इस|े अ|ाप्|प्त|राष|ाष्|्ट्|ट्र|्वत|वतन| उस|राप|त ह|कि | सं|ं औ|हो | दे|किय|ा ।|े प|ार्| भी|करन| न |री |र अ|जाए|क स|ी प|िवा|सभी|्तर|अपन| नि| तथ|तथा|रा |यों|े व|ाओं|ओं |पर |सम्|्री|ीय |सके|व क| द्|द्व|ारा|िए | ऐस|रता| सभ|िक्|ो स|रक्|र प|माज|्या|होग|र उ|ा व|रने| जि|ं म|े म|ाव |ाएग| भा|पने| लि|स्थ|पूर|इस |त क|ाने|रूप|भाव|लिए|े ल|कृत|र्व|ा औ|ो प|द्ध| घो|घोष|श्य|ेश |। इ| रू|ूप |एगा|शिक|े ब|दी | उन|रीय|रति|ूर्|न्य|्ध |णा |ी र|ं स|र्य|य क|परा| पा|े औ|ी अ|ेशो|शों|ानव|ियो|म क| शि| सु|तर्|जो |्र |तिक|सार|चित| पू|ी भ|जिस|ा उ|दिय|राध|चार|र द|विश|स्त|ारी|परि| जन|वाह|नव | बु|म्म|ले |्म |र्ण| जो|ानू|नून|िश्|गी |साम|ोगा|रका|्रो|ोषण|षणा|ाना|ो क|े य| यह|चूं|ूंक|ंकि|अपर|कोई|ोई |ाह |ी म| ।क|ी न|ा ग|ध क|े ज|न स|बन्|निय|याद|ादी|्मा| सद|जीव|हित|य ह|कर |ास |ी ज|ाज |ं न|्था|ामा|कता",
    "mar": "्या|या |त्य|याच|चा | व |ण्य|प्र|कार|ाचा| प्|धिक|िका| अध|च्य|अधि|ार | आह|आहे|हे |ा अ| स्|्रत|स्व|्ये|ा क| कर|्वा|ता |ास | त्|ा स|त्र|ा व|िक |यां|ांच|वा |मान| या|्य | अस| का|रत्|ष्ट|येक|ल्य|र्य|र आ|ाहि|क्ष| सं| को|कोण|ामा|ाच्|ात | रा|ा न|ेका| सा|ून |ंत्| मा|चे |तंत|राष|ाष्|्ट्|ट्र|ने |े स|वात|करण| कि|किं|िंव|ंवा|व्य|ा प|कास|ना | मि| सम|क्त|ये |मिळ|समा|र्व|ातं|्र्|े प| जा|यास|व स|ोणत|ीय |ा आ|रण्|काम| दे|ांन|े क|ा म|रां| व्|्यक|हि |ान | पा|्षण|िळण| आप|ार्|ही |े अ|ा द|ली |ळण्|े व|ची | आण|ंच्| वि|ारा|्रा|ाही|मा |ा ह|द्ध|्री| नि|णे |ला | सर|सर्| नय|नये|ाचे|ी अ|्व |ंना|षण |आपल|ले |माज|बंध|ी प|्त |त क|लेल| हो|ील | शि|शिक|ध्य|ी स|आणि|णि |े ज|देश|न क|ानव|पाह|हिज|िजे|जे |रीय|क स|व त|यक्|ा ज|यात|िक्|त स|े आ|रक्|पल्|वी |संब|ंबं|न्य| ज्|ज्य|स्थ| के|्वत|असल| उप|य अ|क व|त्व|ीत |त व|केल|ाने|य क|णत्|ासा|रति|भाव|े त|व म|ेण्|िष्|साम|क आ|सार|कां|याय|साठ|ाठी|ठी |े य|ंचा|करत|रता|र व|्ती|ीने|याह|र्थ|च्छ|ी आ|स स|ोणा|संर|ंरक|त आ|ंधा|ायद|ी व|ेशा|ित | अश|जाह|हीर|तील|ा ब| अथ|अथव|थवा|ी म|स्त|ा त|ती |नवी|ाची|िवा|देण|याव|ांत|ण म|व आ|य व| हक|हक्|क्क|ा य|ेत |वस्|पूर|ूर्|ारण|द्य|ंचे|ेले|ेल्|कृत|शा |तीन| अर|अर्|्थी|थी |्रद|राध|यत्|ाला|तिष|ष्ठ|श्र|ण स|रून| आल|्ध |सले|े म| शा|्रक|रका|तिक|ाजि|जिक|्क |ाजा| इत|इतर|तो |साध",
    "mai": "ाक |प्र|कार|धिक|िका|ार | आʼ|आʼ |्रत|ेँ |्यक|क अ|िक |्ति| अध|व्य|अधि|क स| प्| व्|क्त|केँ|यक्|तिक|हि | स्|न्त|क व|मे |बाक| सम|मान|त्य|क्ष| छै|छैक|ेक |रत्|स्व|त्र|्ये|येक| अप|ष्ट|सँ |र छ|ैक।| वि| एह|वा |ित |ति |िके|ट्र| जा|्त्|राष|ाष्|्ट्| हो| सा| रा|्य | अन|अपन| कर|।प्|कोन| अछ|अछि|क आ|्वत|वतन|तन्| पर|था | को| वा|ताक|ार्|एहि|पन |ा आ|नहि| मा|्री|समा|नो |रता| दे|्षा|रक |देश|क प| नि| नह| कए| का|छि |न्य|्त |ि क| सं|ोनो| तथ|तथा|्वा|ारक|ान्|ल ज|ा स|ान |िवा|क ह|ीय |र आ| आ |्या|ँ क|वार|ता |ना |जाए| जे|करब| एक| आओ|आओर|ओर |ानव|परि|ँ अ|रीय|ा प|धार|ारण|स्थ|माज|साम|ामा|्रस|र्व|कएल|घोष|अन्|्तर|त क|स्त| सभ|्रा|रण |ँ स|द्ध|एबा|नक |ा अ|िक्|षा |रक्|क।प|ʼ स|चित|पूर|ʼ अ|यक |ाहि|रबा|क ज|कर | घो|ोषण|सम्|र प| हे|हेत|ेतु|तु |शिक|त अ| उप| अव|ूर्|एल |िमे|एहन|हन |षणा|ाधि|सभ |च्छ|अनु| शि|ेल |रूप|क क|भाव|प्त|्ध |ि ज|वक | सक|र अ|रति|निर|िर्|जाह|हो |ँ ए|े स|होए|चार|ण स|र्य|ि आ|सभक|्रक|ाजि|जिक|ाप्|र्ण|त स|क उ|रा |त आ|एत।|त ह| जन|ैक |विव|ोएत|वाध|क ब|री |न प| भा|य आ|राप| ओ |न व|ʼ प|्ण |न अ|कृत|िश्|ा व|कान|ारा|ि स|हु |रसं| उद|उद्|श्य|ाएत|िसँ|जे |ि घ|जेँ| कि|कि |ेश |केओ|ेओ |त्त|सार|क ए|रिव|वास|य प|्थि|विश|ओ व|यता|पर | भे|क ल|नवा| बी| सह|िष्|ि द| रू| ले| पए|पएब| अथ|अथव|थवा|क र|न स|हिम|ास |ए स|ि अ| दो|षाक| पू| द्|द्व|धक ",
    "bho": " के|के |ार |े क|कार|धिक|िका|ओर | आओ|आओर| अध|अधि|े स|ा क|े अ| सं|र क| हो| मे|में|ें |र स|िक | कर|र ह|ा स| से|मान| सम|न क|रा |से |क्ष|े ब|नो |वे | चा|ता |्रा| रा|ति |खे |चाह|ष्ट| सा|राष|ाष्|प्र| का| मा|्ट्|ट्र|े आ| प्| सक| स्| जा| बा|पन |था |त क|ि क|कौन|ौनो|करे|होख| कौ|ेला|्त |ाति|ला |तथा| आप| ओक|आपन|रे |र म| तथ|सबह| हव|हवे|र आ|कर |ोखे|जा |े ओ|तिर|िर |बहि| ह।|ही |सके|केल|ना |हे | और|त्र|ान | खा|खात|।सब| पर|े म|े च|ा आ|षा |ावे|र ब|न स|ओकर|ी क| लो|ाहे|ल ज| सब|्षा|संग|ं क|ित |माज|मिल|े ज|रक्|हिं|िं |ा प|वे।|े ह|ाज |और |स्व|ंत्|ला।|ो स| नइ|नइख|इखे|हु |ानव|िया|्र |लोग|क स|समा|कान|क्त| जे|करा|्रत|े। | ओ |ी स|े न|्री|रीय|पर |े उ|ाही|ानू|नून|स्थ|े व|ाम |्वत|वतं|तंत|रता|केह|या |े ख|। स| सु|प्त| दे|े त|साम|र अ|ीय |र प|बा।|ा।स|सभे|भे | वि|योग|दी | आद|ून |ा म|्य |व्य|ए क|ेहु| या|री |र न| बि|राप|ाप्|ु क| मि|यता|आदि|दिम|मी |नवा|ाधि|े द|चार|ले | नि| पा|ोग | ही| दो|ादी|हि |तिक|पूर| इ |ा ह|्ति|ल ह|खल |ाव | अप| सभ|िमी|देश|ुक्| सह|शिक|िक्|ि म|जे |षण |ाजि|जिक|क आ|्तर|े प| उप|जाद|े भ|्या| जर|म क|ेकर| अं|े र|।के|न आ|सब |साथ|ंगठ|गठन|ठन |रो | जी|ा। |्म |ी ब|हो |न ह|े ल|न म|वाध|निय|ेश | शि|ज क| ले|ने |बा |संर|ंरक|्षण|ामा|य क|ास |उपय|पयो|दोस| आज|आजा| भी| उच|चित|र व| पू| घो|घोष| व्| शा|िल |ा।क| कई| को|होए|्थि",
    "nep": "को | र |कार|प्र|ार |ने |िका|क्त|्यक|धिक|व्य| गर| प्|्रत|अधि|्ति| अध| व्|यक्|मा |िक |ाई |त्य|न्त|लाई|मान| सम|त्र|गर्|र्न|क व|्ने| वा|वा | स्|रत्|र स|्ये|येक|ेक |छ ।|तिल|हरू|क्ष|ो स| वि|ा स|्त्|िला| । |स्व|हुन|ति | हु| मा| रा|ले |र छ| छ |ष्ट|समा|वतन|तन्|्ट्|ट्र| सं|ो अ|राष|ाष्|्वत|नेछ|ुने|ान |े अ|ता | का|्र |हरु|गरि|ाको|िने| अन|ना | नि|े छ| सा|क स|तिक|ित |नै |र र|रता|रू |था |ा र|कुन|ुनै|ा अ|स्त|्त | छै|छैन| तथ|तथा|ा प|ार्|वार| पर|ा व|एको|्षा|परि|रक्|। प|माज|रु |द्ध|का |्या|ो प|ामा|्रा|सको|ेछ | ला|धार|नि |ाहर|देश| यस|र ह|िवा|सबै|र म|भाव|्य |र व|रहर|रको|न अ|सम्|े र|संर|ंरक|अन्|ताक|्रि|्वा|ा भ|त र| कु| त्|री |ो व|न स|रिन|लाग|ारक|ानव| सब| शि|शिक|िक्|ै व|रिय|रा |ा न|पनि|ारा|श्य|ा त|्यस|यस्|ाउन|्न | अप|चार|ाव | भए|ारम| सु|ुद्|षा |ि र|रूक| सह|बाट|्षण|साम|्तर|िय |रति|ो आ|र प|ो ल|कान|द्व|ुक्|ान्| उप|द्द|ुन |ैन |ेछ।|ैन।|ारह| भे|ागि|गि |निज|वाह|्ध |र्य| आध|रमा|ा म|नको|बै |न ग|ाट |।प्|ाजि|जिक|त्प|िको|ाय |र त|ात्| उस|ूर्| अभ| अर|जको|स्थ| आव|त स|ित्| पन|िएक|्तो|तो | पा|ा ग| भन|ानु|परा|राध| छ।| मत|अपर|भेद|ि स|रुद|ो ह|रिव|रका|न्य| जन|यता|े स|र्म|ारी| दि|क अ|नमा|ूको|हित|ा क|क र|र अ|ा ब|उसक|पूर|त्व|र्द|सार|णको|युक|।कु|विध| घो|घोष| सक|भएक|नुन|्यह|ि व|ो भ| पु| मन|नी |विच| दे|राज|विर|िरु|काम|र न|यहर|िश्"
  }
}

},{}],3:[function(require,module,exports){
// This file is generated by `build.js`.
module.exports = {
  cmn: /[\u2E80-\u2E99\u2E9B-\u2EF3\u2F00-\u2FD5\u3005\u3007\u3021-\u3029\u3038-\u303B\u3400-\u4DB5\u4E00-\u9FEF\uF900-\uFA6D\uFA70-\uFAD9]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0]|\uD87E[\uDC00-\uDE1D]/g,
  Latin: /[A-Za-z\xAA\xBA\xC0-\xD6\xD8-\xF6\xF8-\u02B8\u02E0-\u02E4\u1D00-\u1D25\u1D2C-\u1D5C\u1D62-\u1D65\u1D6B-\u1D77\u1D79-\u1DBE\u1E00-\u1EFF\u2071\u207F\u2090-\u209C\u212A\u212B\u2132\u214E\u2160-\u2188\u2C60-\u2C7F\uA722-\uA787\uA78B-\uA7BF\uA7C2-\uA7C6\uA7F7-\uA7FF\uAB30-\uAB5A\uAB5C-\uAB64\uAB66\uAB67\uFB00-\uFB06\uFF21-\uFF3A\uFF41-\uFF5A]/g,
  Cyrillic: /[\u0400-\u0484\u0487-\u052F\u1C80-\u1C88\u1D2B\u1D78\u2DE0-\u2DFF\uA640-\uA69F\uFE2E\uFE2F]/g,
  Arabic: /[\u0600-\u0604\u0606-\u060B\u060D-\u061A\u061C\u061E\u0620-\u063F\u0641-\u064A\u0656-\u066F\u0671-\u06DC\u06DE-\u06FF\u0750-\u077F\u08A0-\u08B4\u08B6-\u08BD\u08D3-\u08E1\u08E3-\u08FF\uFB50-\uFBC1\uFBD3-\uFD3D\uFD50-\uFD8F\uFD92-\uFDC7\uFDF0-\uFDFD\uFE70-\uFE74\uFE76-\uFEFC]|\uD803[\uDE60-\uDE7E]|\uD83B[\uDE00-\uDE03\uDE05-\uDE1F\uDE21\uDE22\uDE24\uDE27\uDE29-\uDE32\uDE34-\uDE37\uDE39\uDE3B\uDE42\uDE47\uDE49\uDE4B\uDE4D-\uDE4F\uDE51\uDE52\uDE54\uDE57\uDE59\uDE5B\uDE5D\uDE5F\uDE61\uDE62\uDE64\uDE67-\uDE6A\uDE6C-\uDE72\uDE74-\uDE77\uDE79-\uDE7C\uDE7E\uDE80-\uDE89\uDE8B-\uDE9B\uDEA1-\uDEA3\uDEA5-\uDEA9\uDEAB-\uDEBB\uDEF0\uDEF1]/g,
  ben: /[\u0980-\u0983\u0985-\u098C\u098F\u0990\u0993-\u09A8\u09AA-\u09B0\u09B2\u09B6-\u09B9\u09BC-\u09C4\u09C7\u09C8\u09CB-\u09CE\u09D7\u09DC\u09DD\u09DF-\u09E3\u09E6-\u09FE]/g,
  Devanagari: /[\u0900-\u0950\u0955-\u0963\u0966-\u097F\uA8E0-\uA8FF]/g,
  jpn: /[\u3041-\u3096\u309D-\u309F]|\uD82C[\uDC01-\uDD1E\uDD50-\uDD52]|\uD83C\uDE00|[\u30A1-\u30FA\u30FD-\u30FF\u31F0-\u31FF\u32D0-\u32FE\u3300-\u3357\uFF66-\uFF6F\uFF71-\uFF9D]|\uD82C[\uDC00\uDD64-\uDD67]|[㐀-䶵一-龯]/g,
  kor: /[\u1100-\u11FF\u302E\u302F\u3131-\u318E\u3200-\u321E\u3260-\u327E\uA960-\uA97C\uAC00-\uD7A3\uD7B0-\uD7C6\uD7CB-\uD7FB\uFFA0-\uFFBE\uFFC2-\uFFC7\uFFCA-\uFFCF\uFFD2-\uFFD7\uFFDA-\uFFDC]/g,
  tel: /[\u0C00-\u0C0C\u0C0E-\u0C10\u0C12-\u0C28\u0C2A-\u0C39\u0C3D-\u0C44\u0C46-\u0C48\u0C4A-\u0C4D\u0C55\u0C56\u0C58-\u0C5A\u0C60-\u0C63\u0C66-\u0C6F\u0C77-\u0C7F]/g,
  tam: /[\u0B82\u0B83\u0B85-\u0B8A\u0B8E-\u0B90\u0B92-\u0B95\u0B99\u0B9A\u0B9C\u0B9E\u0B9F\u0BA3\u0BA4\u0BA8-\u0BAA\u0BAE-\u0BB9\u0BBE-\u0BC2\u0BC6-\u0BC8\u0BCA-\u0BCD\u0BD0\u0BD7\u0BE6-\u0BFA]|\uD807[\uDFC0-\uDFF1\uDFFF]/g,
  guj: /[\u0A81-\u0A83\u0A85-\u0A8D\u0A8F-\u0A91\u0A93-\u0AA8\u0AAA-\u0AB0\u0AB2\u0AB3\u0AB5-\u0AB9\u0ABC-\u0AC5\u0AC7-\u0AC9\u0ACB-\u0ACD\u0AD0\u0AE0-\u0AE3\u0AE6-\u0AF1\u0AF9-\u0AFF]/g,
  kan: /[\u0C80-\u0C8C\u0C8E-\u0C90\u0C92-\u0CA8\u0CAA-\u0CB3\u0CB5-\u0CB9\u0CBC-\u0CC4\u0CC6-\u0CC8\u0CCA-\u0CCD\u0CD5\u0CD6\u0CDE\u0CE0-\u0CE3\u0CE6-\u0CEF\u0CF1\u0CF2]/g,
  mal: /[\u0D00-\u0D03\u0D05-\u0D0C\u0D0E-\u0D10\u0D12-\u0D44\u0D46-\u0D48\u0D4A-\u0D4F\u0D54-\u0D63\u0D66-\u0D7F]/g,
  mya: /[\u1000-\u109F\uA9E0-\uA9FE\uAA60-\uAA7F]/g,
  ori: /[\u0B01-\u0B03\u0B05-\u0B0C\u0B0F\u0B10\u0B13-\u0B28\u0B2A-\u0B30\u0B32\u0B33\u0B35-\u0B39\u0B3C-\u0B44\u0B47\u0B48\u0B4B-\u0B4D\u0B56\u0B57\u0B5C\u0B5D\u0B5F-\u0B63\u0B66-\u0B77]/g,
  pan: /[\u0A01-\u0A03\u0A05-\u0A0A\u0A0F\u0A10\u0A13-\u0A28\u0A2A-\u0A30\u0A32\u0A33\u0A35\u0A36\u0A38\u0A39\u0A3C\u0A3E-\u0A42\u0A47\u0A48\u0A4B-\u0A4D\u0A51\u0A59-\u0A5C\u0A5E\u0A66-\u0A76]/g,
  amh: /[\u1200-\u1248\u124A-\u124D\u1250-\u1256\u1258\u125A-\u125D\u1260-\u1288\u128A-\u128D\u1290-\u12B0\u12B2-\u12B5\u12B8-\u12BE\u12C0\u12C2-\u12C5\u12C8-\u12D6\u12D8-\u1310\u1312-\u1315\u1318-\u135A\u135D-\u137C\u1380-\u1399\u2D80-\u2D96\u2DA0-\u2DA6\u2DA8-\u2DAE\u2DB0-\u2DB6\u2DB8-\u2DBE\u2DC0-\u2DC6\u2DC8-\u2DCE\u2DD0-\u2DD6\u2DD8-\u2DDE\uAB01-\uAB06\uAB09-\uAB0E\uAB11-\uAB16\uAB20-\uAB26\uAB28-\uAB2E]/g,
  tha: /[\u0E01-\u0E3A\u0E40-\u0E5B]/g,
  sin: /[\u0D82\u0D83\u0D85-\u0D96\u0D9A-\u0DB1\u0DB3-\u0DBB\u0DBD\u0DC0-\u0DC6\u0DCA\u0DCF-\u0DD4\u0DD6\u0DD8-\u0DDF\u0DE6-\u0DEF\u0DF2-\u0DF4]|\uD804[\uDDE1-\uDDF4]/g,
  ell: /[\u0370-\u0373\u0375-\u0377\u037A-\u037D\u037F\u0384\u0386\u0388-\u038A\u038C\u038E-\u03A1\u03A3-\u03E1\u03F0-\u03FF\u1D26-\u1D2A\u1D5D-\u1D61\u1D66-\u1D6A\u1DBF\u1F00-\u1F15\u1F18-\u1F1D\u1F20-\u1F45\u1F48-\u1F4D\u1F50-\u1F57\u1F59\u1F5B\u1F5D\u1F5F-\u1F7D\u1F80-\u1FB4\u1FB6-\u1FC4\u1FC6-\u1FD3\u1FD6-\u1FDB\u1FDD-\u1FEF\u1FF2-\u1FF4\u1FF6-\u1FFE\u2126\uAB65]|\uD800[\uDD40-\uDD8E\uDDA0]|\uD834[\uDE00-\uDE45]/g
}

},{}],4:[function(require,module,exports){
// This file is generated by `build.js`
'use strict'

/* Load `trigram-utils`. */
var utilities = require('trigram-utils')

/* Load `expressions` (regular expressions matching
 * scripts). */
var expressions = require('./expressions.js')

/* Load `data` (trigram information per language,
 * per script). */
var data = require('./data.json')

/* Expose `detectAll` on `detect`. */
detect.all = detectAll

/* Expose `detect`. */
module.exports = detect

/* Maximum sample length. */
var MAX_LENGTH = 2048

/* Minimum sample length. */
var MIN_LENGTH = 10

/* The maximum distance to add when a given trigram does
 * not exist in a trigram dictionary. */
var MAX_DIFFERENCE = 300

/* Construct trigram dictionaries. */
;(function() {
  var languages
  var name
  var trigrams
  var model
  var script
  var weight

  for (script in data) {
    languages = data[script]

    for (name in languages) {
      model = languages[name].split('|')

      weight = model.length

      trigrams = {}

      while (weight--) {
        trigrams[model[weight]] = weight
      }

      languages[name] = trigrams
    }
  }
})()

/**
 * Get the most probable language for the given value.
 *
 * @param {string} value - The value to test.
 * @param {Object} options - Configuration.
 * @return {string} The most probable language.
 */
function detect(value, options) {
  return detectAll(value, options)[0][0]
}

/**
 * Get a list of probable languages the given value is
 * written in.
 *
 * @param {string} value - The value to test.
 * @param {Object} options - Configuration.
 * @return {Array.<Array.<string, number>>} An array
 *   containing language--distance tuples.
 */
function detectAll(value, options) {
  var settings = options || {}
  var minLength = MIN_LENGTH
  var only = [].concat(settings.whitelist || [], settings.only || [])
  var ignore = [].concat(settings.blacklist || [], settings.ignore || [])
  var script

  if (settings.minLength !== null && settings.minLength !== undefined) {
    minLength = settings.minLength
  }

  if (!value || value.length < minLength) {
    return und()
  }

  value = value.slice(0, MAX_LENGTH)

  /* Get the script which characters occur the most
   * in `value`. */
  script = getTopScript(value, expressions)

  /* One languages exists for the most-used script. */
  if (!(script[0] in data)) {
    /* If no matches occured, such as a digit only string,
     * or because the language is ignored, exit with `und`. */
    if (script[1] === 0 || !allow(script[0], only, ignore)) {
      return und()
    }

    return singleLanguageTuples(script[0])
  }

  /* Get all distances for a given script, and
   * normalize the distance values. */
  return normalize(
    value,
    getDistances(utilities.asTuples(value), data[script[0]], only, ignore)
  )
}

/**
 * Normalize the difference for each tuple in
 * `distances`.
 *
 * @param {string} value - Value to normalize.
 * @param {Array.<Array.<string, number>>} distances
 *   - List of distances.
 * @return {Array.<Array.<string, number>>} - Normalized
 *   distances.
 */
function normalize(value, distances) {
  var min = distances[0][1]
  var max = value.length * MAX_DIFFERENCE - min
  var index = -1
  var length = distances.length

  while (++index < length) {
    distances[index][1] = 1 - (distances[index][1] - min) / max || 0
  }

  return distances
}

/**
 * From `scripts`, get the most occurring expression for
 * `value`.
 *
 * @param {string} value - Value to check.
 * @param {Object.<RegExp>} scripts - Top-Scripts.
 * @return {Array} Top script and its
 *   occurrence percentage.
 */
function getTopScript(value, scripts) {
  var topCount = -1
  var topScript
  var script
  var count

  for (script in scripts) {
    count = getOccurrence(value, scripts[script])

    if (count > topCount) {
      topCount = count
      topScript = script
    }
  }

  return [topScript, topCount]
}

/**
 * Get the occurrence ratio of `expression` for `value`.
 *
 * @param {string} value - Value to check.
 * @param {RegExp} expression - Code-point expression.
 * @return {number} Float between 0 and 1.
 */
function getOccurrence(value, expression) {
  var count = value.match(expression)

  return (count ? count.length : 0) / value.length || 0
}

/**
 * Get the distance between an array of trigram--count
 * tuples, and multiple trigram dictionaries.
 *
 * @param {Array.<Array.<string, number>>} trigrams - An
 *   array containing trigram--count tuples.
 * @param {Object.<Object>} languages - multiple
 *   trigrams to test against.
 * @param {Array.<string>} only - Allowed languages; if
 *   non-empty, only included languages are kept.
 * @param {Array.<string>} ignore - Disallowed languages;
 *   included languages are ignored.
 * @return {Array.<Array.<string, number>>} An array
 *   containing language--distance tuples.
 */
function getDistances(trigrams, languages, only, ignore) {
  var distances = []
  var language

  languages = filterLanguages(languages, only, ignore)

  for (language in languages) {
    distances.push([language, getDistance(trigrams, languages[language])])
  }

  return distances.length === 0 ? und() : distances.sort(sort)
}

/**
 * Get the distance between an array of trigram--count
 * tuples, and a language dictionary.
 *
 * @param {Array.<Array.<string, number>>} trigrams - An
 *   array containing trigram--count tuples.
 * @param {Object.<number>} model - Object
 *   containing weighted trigrams.
 * @return {number} - The distance between the two.
 */
function getDistance(trigrams, model) {
  var distance = 0
  var index = -1
  var length = trigrams.length
  var trigram
  var difference

  while (++index < length) {
    trigram = trigrams[index]

    if (trigram[0] in model) {
      difference = trigram[1] - model[trigram[0]] - 1

      if (difference < 0) {
        difference = -difference
      }
    } else {
      difference = MAX_DIFFERENCE
    }

    distance += difference
  }

  return distance
}

/**
 * Filter `languages` by removing languages in
 * `ignore`, or including languages in `only`.
 *
 * @param {Object.<Object>} languages - Languages
 *   to filter
 * @param {Array.<string>} only - Allowed languages; if
 *   non-empty, only included languages are kept.
 * @param {Array.<string>} ignore - Disallowed languages;
 *   included languages are ignored.
 * @return {Object.<Object>} - Filtered array of
 *   languages.
 */
function filterLanguages(languages, only, ignore) {
  var filteredLanguages
  var language

  if (only.length === 0 && ignore.length === 0) {
    return languages
  }

  filteredLanguages = {}

  for (language in languages) {
    if (allow(language, only, ignore)) {
      filteredLanguages[language] = languages[language]
    }
  }

  return filteredLanguages
}

/**
 * Check if `language` can match according to settings.
 *
 * @param {string} language - Languages
 *   to filter
 * @param {Array.<string>} only - Allowed languages; if
 *   non-empty, only included languages are kept.
 * @param {Array.<string>} ignore - Disallowed languages;
 *   included languages are ignored.
 * @return {boolean} - Whether `language` can match
 */
function allow(language, only, ignore) {
  if (only.length === 0 && ignore.length === 0) {
    return true
  }

  return (
    (only.length === 0 || only.indexOf(language) !== -1) &&
    ignore.indexOf(language) === -1
  )
}

/* Create a single `und` tuple. */
function und() {
  return singleLanguageTuples('und')
}

/* Create a single tuple as a list of tuples from a given
 * language code. */
function singleLanguageTuples(language) {
  return [[language, 1]]
}

/* Deep regular sort on the number at `1` in both objects. */
function sort(a, b) {
  return a[1] - b[1]
}

},{"./data.json":2,"./expressions.js":3,"trigram-utils":6}],5:[function(require,module,exports){
'use strict'

module.exports = nGram

nGram.bigram = nGram(2)
nGram.trigram = nGram(3)

// Factory returning a function that converts a value string to n-grams.
function nGram(n) {
  if (typeof n !== 'number' || isNaN(n) || n < 1 || n === Infinity) {
    throw new Error('`' + n + '` is not a valid argument for n-gram')
  }

  return grams

  // Create n-grams from a given value.
  function grams(value) {
    var nGrams = []
    var index

    if (value === null || value === undefined) {
      return nGrams
    }

    value = value.slice ? value : String(value)
    index = value.length - n + 1

    if (index < 1) {
      return nGrams
    }

    while (index--) {
      nGrams[index] = value.slice(index, index + n)
    }

    return nGrams
  }
}

},{}],6:[function(require,module,exports){
'use strict'

var trigram = require('n-gram').trigram
var collapse = require('collapse-white-space')
var trim = require('trim')

var has = {}.hasOwnProperty

exports.clean = clean
exports.trigrams = getCleanTrigrams
exports.asDictionary = getCleanTrigramsAsDictionary
exports.asTuples = getCleanTrigramsAsTuples
exports.tuplesAsDictionary = getCleanTrigramTuplesAsDictionary

// Clean `value`/
// Removed general non-important (as in, for language detection) punctuation
// marks, symbols, and numbers.
function clean(value) {
  if (value === null || value === undefined) {
    return ''
  }

  return trim(
    collapse(String(value).replace(/[\u0021-\u0040]+/g, ' '))
  ).toLowerCase()
}

// Get clean, padded, trigrams.
function getCleanTrigrams(value) {
  return trigram(' ' + clean(value) + ' ')
}

// Get an `Object` with trigrams as its attributes, and their occurence count as
// their values.
function getCleanTrigramsAsDictionary(value) {
  var trigrams = getCleanTrigrams(value)
  var index = trigrams.length
  var dictionary = {}
  var trigram

  while (index--) {
    trigram = trigrams[index]

    if (has.call(dictionary, trigram)) {
      dictionary[trigram]++
    } else {
      dictionary[trigram] = 1
    }
  }

  return dictionary
}

// Get an `Array` containing trigram--count tuples from a given value.
function getCleanTrigramsAsTuples(value) {
  var dictionary = getCleanTrigramsAsDictionary(value)
  var tuples = []
  var trigram

  for (trigram in dictionary) {
    tuples.push([trigram, dictionary[trigram]])
  }

  tuples.sort(sort)

  return tuples
}

// Get an `Array` containing trigram--count tuples from a given value.
function getCleanTrigramTuplesAsDictionary(tuples) {
  var index = tuples.length
  var dictionary = {}
  var tuple

  while (index--) {
    tuple = tuples[index]
    dictionary[tuple[0]] = tuple[1]
  }

  return dictionary
}

// Deep regular sort on item at `1` in both `Object`s.
function sort(a, b) {
  return a[1] - b[1]
}

},{"collapse-white-space":1,"n-gram":5,"trim":7}],7:[function(require,module,exports){

exports = module.exports = trim;

function trim(str){
  return str.replace(/^\s*|\s*$/g, '');
}

exports.left = function(str){
  return str.replace(/^\s*/, '');
};

exports.right = function(str){
  return str.replace(/\s*$/, '');
};

},{}]},{},[4])(4)
});
