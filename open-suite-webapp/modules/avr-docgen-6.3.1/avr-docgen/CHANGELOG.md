## v6.3.1 - 2022-10-18
**Updated:**
* Migration vers Axelor 6.3.1

## v4.1.14 - 2022-09-22
**Updated:**
* Upgrade to axelor 6.1.20

## v4.1.6  - 2022-04-12
**Added:**
* Initialisation du changelog
* Envoi de mail avec pièce-jointe
* Génération de pdf depuis un template Word (voir configuration Docgen dans Axelor)
* Gestion des factures et avoirs des fournisseurs (+ configuration module)
* Configuration Docgen par champ et liaison (bind) par le titre dans le template Word
* Communication avec le serveur Docgen
* Possibilité de tester son template avec un bouton  (dans le menu Docgen > Templates)
* Exportation / Importation des configurations Docgen (dans le menu Docgen > Templates)
* Multi-langue supporté (récupère la langue du client à qui on souhaite l'envoyer)
* Possibilité de modifier l'ordre de récupèration des éléments (lorsque c'est une liste) + Ascendant / Descendant
* Possibilité d'envoi d'images
* Possibilité d'envoi de texte au format HTML (Attention Aspose gère mal beaucoup d'éléments HTML !!!)
* Choix du nombre de décimal à récupérer.
* Possibilité de faire du HQL et du SQL pour la récupération d'éléments.

**Fixed:**
* Fix problem of name changed for dependencies : avr-docgen --> docgen
* Check if partner is null if is required when generate file