L3 - INFO Générale - ALGO 6 PROJET SOKOBAN

ABDELKADER Youssef - MASCARENHAS Rafael

Pour lancer le programme avec notre IA, il suffit de s'assurer que la variable:
    > public static final String IA
Dans le fichier src/Global/Configuration.java, a pour valeur la chaine "MagIA". 

--------------------------------------------------------------------------------

Dans le dossier res, vous trouverez un ensemble de test (niveaux Sokoban) 
différents avec lesquelles vous pouvez tester notre IA. 

Pour lancer le programme avec un fichier de test spécifique, il suffit de 
changer cette variable dans le fichier Sokoban.java (le main):
    in = Configuration.ouvre("Niveaux/filename.txt");
En remplacant filename.txt par le fichier désiré.

    - TestsH1.txt
    Ce sont des tests que fonctionnent bien avec l'heuristique I de l'IA MagIA

    - TestsH2.txt
    Ce sont des tests que fonctionnent bien avec l'heuristique II de l'IA MagIA

--------------------------------------------------------------------------------

Pour aller plus loin...

On a aussi essayer d'ameliorer l'estrategie du MagIA. C'est comme MagIAII est 
venu au monde.
Pour le moment cet IA ne generere pas de coups mais affiche les movements des
caisses necessaires à atteindre un état de jeu fial.
L'affichage comporte à chaque étape quel case le pousseur doit se placer et dans
quelle direction il faut se deplacer

Malhereusement on a pas eu le temps de finir cette implementation que semble 
être bien plus efficace.