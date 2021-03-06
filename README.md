[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)

An implementation of CKTAGA (**C**entered **K**ernel **T**arget **A**lignment for **G**enetic **A**lgorithms) applied to Inductive Logic Programming (ILP) is contained here. The code requires Yap prolog and Aleph to run. 

The algorithms implemented herein are described in the following dissertation: https://digitalcommons.uri.edu/oa_diss/1090/.

Abstract from the above dissertation (with emphasis added): This study aimed both to apply centered kernel target alignment (CKTA) to inductive logic programming (ILP) in several different ways and to apply a complete refinement operator in a practical setting. A new genetic algorithm (GA) results from the research, utilizing a complete, locally finite refinement operator and also incorporating **CKTA both as a *fitness score* and as a means for the *promotion of diversity***. As a fitness score, CKTA can either be used standalone or as a contributor to a hybrid score which utilizes the accuracy (weighted or normal) of the learned logic hypothesis as well. In terms of diversity promotion, CKTA is used for incest avoidance and as a means for creating diverse ensembles. This is the first study to employ CKTA for diversity promotion of any kind. It is also the first to apply CKTA to ILP. The kernels in this study are created via dynamic propositionalization, where the features are learned jointly with the kernel to be used for classification via a genetic algorithm. In this sense, genetic kernels for ILP are created. The results show that the methods proposed herein are promising, encouraging future work. It is worth noting that the applications of CKTA in this study are not specific to ILP. They can also be used more generally in any other domain using kernels.

The parser for java interfacing to Aleph is based on Igor Maznitsa's very well written prolog parser which can be found here: https://github.com/raydac/java-prolog-parser. The parser was mildly updated to support Aleph idiosyncrocies.

Future notes regarding usage will be forthcoming.
