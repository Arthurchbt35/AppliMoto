# Projet Android : Suivi de Saison MotoGP 2026

## C'est quoi ce projet ?
C'est une application Android créée pour notre projet de cours. Le but est d'avoir une application simple et pratique pour suivre toute la saison 2026 de MotoGP. Elle permet de voir les classements, le calendrier des courses et le détail de chaque Grand Prix. Chaque utilisateur pourra choisir son écurie et ses deux pilotes favoris.

## D'où viennent les données (L'API) ?
Pour récupérer les informations de la saison 2026, nous utilisons une API (une base de données en ligne). Nous avons choisi la **MotoGP** (disponible en version gratuite sur RapidAPI) qui nous donne les dates, les noms des circuits et les résultats.

*Notre petite astuce :* Comme les API gratuites bloquent parfois quand on fait trop de demandes d'un coup, nous avons aussi créé notre propre fichier de secours (un fichier JSON hébergé sur GitHub). Ça fonctionne exactement comme l'API et ça nous garantit que l'application marchera parfaitement le jour de la présentation !

## Comment est fait le calendrier ?
Le calendrier est une liste dynamique. Quand l'application s'ouvre, elle va lire les données de l'API et crée automatiquement une liste déroulante avec toutes les courses de l'année.
C'est un affichage intelligent : si on clique sur une course qui n'a pas encore eu lieu, l'application montre seulement le tracé du circuit. Mais si on clique sur une course qui est déjà terminée, elle affiche le circuit, les résultats complets et la différence entre la position de départ (qualifications) et la position à l'arrivée.

## Les graphiques d'évolution (Jetpack Compose)
Pour que l'application soit plus visuelle, nous avons ajouté un graphique qui montre l'évolution des points des meilleurs pilotes du championnat course après course.
Pour dessiner ce graphique, **nous utilisons la fonction Jetpack Compose**. C'est la nouvelle méthode d'Android pour créer des écrans. C'est beaucoup plus simple et moderne pour construire de jolies courbes de points que les vieilles méthodes habituelles !