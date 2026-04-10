
# TD 2 — API REST fonctionnelle (TODO list)

## 🎯 Objectifs du TD
À la fin de ce TD, vous saurez :
- mettre en place les bonnes pratiques de développement sur un projet simple (KISS, DRY, etc.),
- organiser une API REST suivant une architecture en 3 couches,
- construire une Javadoc et des logs pertinents,
- mettre en place une gestion des erreurs

---

## 🧭 Étapes du TD
### Étape 0 — Reprenez votre projet là où vous en étiez
1. Vérifiez que votre API REST fonctionne toujours :)
2. Pour la suite du TD, veuillez tenir compte des bonnes pratiques de développement vues en cours (DRY, SRP, KISS, etc.).
3. Pensez également à compléter la Javadoc et à ajouter des logs lorsque cela est pertinent.

### Étape 1 — Mise en application d'une architecture en 3 couches
1. Refactorer les packages.
2. Tester chaque endpoint afin de détecter d'éventuelles régressions.

### Étape 2 — Connexion JDBC et SQLite
1. Dans le `pom.xml`, ajouter la dépendance suivante. Il s'agit du driver **SQLite** pour la future connexion JDBC.
   ```xml
       <dependency>
          <groupId>org.xerial</groupId>
          <artifactId>sqlite-jdbc</artifactId>
          <version>3.45.2.0</version>
       </dependency>
   ```
2. Dans la classe `TaskDao`, remplacez la `Map` par une base de données SQLite. Implémentez les connexions JDBC nécessaires au fonctionnement de chaque endpoint. Pensez à initialiser la création de votre table si elle n'existe pas.

### Étape 3 — Ajout de DTO (Data Transfert Object)
1. Lire cet article https://code-garage.com/blog/a-quoi-servent-les-data-transfer-objects-dto
2. Mettre en place des DTO dans votre projet tel que :
* `POST /tasks` prenne en paramètre une tâche avec un titre et une description. L'id sera autogénéré par SQLite à l'insertion et `done` initialisé à `false`. La taille maximale du titre est de 50 caractères et celle de la description est de 255 caractères.
* `PUT /tasks/{id}` prenne en paramètre une tâche avec un titre, une description et un statut (`done`). La taille maximale du titre est de 50 caractères et celle de la description est de 255 caractères.
* Tous les endpoints permettant de consulter des tâches retournent un DTO. Les champs du DTO seront les mêmes que ceux du model actuellement utilisé. 
3. Mettre en place la validation des DTO dans la couche Présentation. Retourner un code (`400`) en cas de paramètre invalide, ainsi qu'un DTO d'erreur contenant le nom du champ en erreur et une description. 
4. Compléter le swagger et mettre à jour les paramètres dans votre collection Postman.

### Étape 4 — Gestion des erreurs
1. Retourner un code (`500`) si une erreur inattendue se produit (try-catch des exceptions). 

### Étape 5 — Terminer le TD1
1. S'il n'est pas terminé, continuer l'implémentation des endpoints manquants.

### Étape 6 — Commit & push de votre travail
Exporter la collection Postman puis l'ajouter à la racine de votre projet.
Pousser l'ensemble de vos travaux (collection Postman comprise) sur votre repo distant.
  
**La version rendue à la fin de ce TD fera l'objet d'une note.**
* Collection Postman
* Swagger
* Code

## Étape bonus
1. Mettre en place des tests unitaires simples sur des classes sans dépendances.

## 🧠 Mémo Git (commandes utilisées)
```bash
# Cloner le dépôt (HTTPS)
git clone URL_DU_REPO

# Créer une branche et s'y positionner
git checkout -b nom-branche

# Voir l'état local
git status

# Indexer tous les fichiers modifiés
git add .

# Commit (message court, verbe à l'infinitif/impératif)
git commit -m "feat: implement POST and DELETE for tasks"

# Définir le remote de suivi et pousser la première fois
git push -u origin nom-branche

# Pousser les commits suivants
git push

# Récupérer les changements distants (si besoin)
git pull

# Voir les remotes configurés
git remote -v
```

---

_Bon TD !_
