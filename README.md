my-shopping-cart
=============
A follow along implementation of a shopping cart based on "Practical FP in Scala" book.

## Stack
* Scala
* Typelevel Cats, Cats Effect 3
* Http4s
* Skunk
* Redis, Postgres

## Tests
```
sbt test
```

Integration tests:

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
