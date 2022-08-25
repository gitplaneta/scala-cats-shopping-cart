shopping-cart
=============

## Tests
```
sbt test
```

Itegration tests:

```
docker-compose up
sbt it:test
docker-compose down
```

## Build Docker image

```
sbt docker:publishLocal
```

## Run
```
cd app && docker-compose up
```
