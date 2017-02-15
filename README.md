# Google HashCode 2017 Practice Problem

This repository contains my workings for attempting to solve the HashCode 2017 practice problem.
It is coded in Java and uses a Genetic Algorithm to figure out which is the fittest Pizza!

## Starting Out

The clock has struck HashCode o'clock and now you are faced with a real life problem you must solve using your coding skills and problem solving abilities.

You've stepped up to the plate faced with the challenge of splitting a Pizza between your many friends. Simple right?
Not unless you need to ensure that every friend gets a quadrliateral piece of the hot goodness no larger than `H` and with not less that `L` toppings.

As featured in the book The Hitchiker's Guide to the Galaxy by Douglas Adams the phrase Don't Panic should help you to soothe your nerves.
This is a big challenge and you're more than capable of solving it, you turned up and now it's your time to shine.

## Break It Down

Looking at any problem try to break the problem down in to Classes (People, Drones, Pizza, Slices, Cats) and what functions or what variables they must store.
In our case we are just looking to look at `Pizza` and their many `Slice` at different points we can dish out.

```java

public class Pizza {
  public List<Slice> slices; /* Store all the slices for this Pizza cutting scenario */
}

public class Slice {
  public int fromX, fromY; /* Slice is from here */
  public int toX, toY; /* to here */
}

```

Now you've represented the problem using Java objects.
We can now create instances of the `Pizza` with different scenarios (selections) to `Slice` it up.

## Get Them Pizza Datas

You can pull out what our `Pizza` could look like by reading the file line by line.
The first line tells us that we have `rows columns min_topping max_slice` which we store as variables for later use.
The following lines will tell us where on a (Y, X) our tomatos and mushrooms live.

See `HashCodeSolver.loadFile()` for more details and `HashCodeSolver.storeFile()` for simple saving of our 'output' (arrangement).

## Determining Fitness

Genetic Algorithms are simple and nature inspired.
You take a sequence that is deemed the fittest (most appropriate) and breed it with a similarly suited mate.
This should mean your baby `Pizza` is more likely to be fitter and stronger than you are? Yes and no.
We know that the `Pizza` must have a set number of ingredients in a `Slice`, must not be bigger than a certain slice, each `Slice` must not conflict with another.
Because Barbara is definitely going to be mad if you stole her allocated mushroom!
You can if you like, not divide the entire Pizza but best to share the most of it with you and your mates.

These restrictions will be part of your fitness calculation.
`fitness = no overlap, minimum toppings provided, not too much for a single person and most of your pizza is shared`

See `Pizza.fitness()` for more details.

## How Big a Slice Exactly?

You can generate a list of factors based on a minimum and maximum number of cells per quadrliateral slice in your set row and columns sized pizza.
The maximum is given for us as a value in the input file and the minimum will be `number of different ingredients * minimum number of ingredients`.
Loop from the 1 to the size of the Pizza for both rows and columns, call these variables Y and X respectively.
Multiply your X and Y and ensure that it is within the minimum and maximum cell bounds.
If it is then add it to a list of valid factors (or pizza slices sizes).

See `Utility.factors()` for more details.

## Let's Cut!

Hold on there... are you sure that you're going to cut the `Pizza` that we have the minimum number of ingredients?
We can now loop through each possible position from X to Y and ensure that if we cut here we'd meet the minimum and we won't be cutting into pizza space that does not exist.

See `HashCode.slices()` for more details

## Right, It's Cold Now...

Tough, you've paid the Delivery Guy or Girl for the Pizza, you're just going to have to cut and reheat (or eat it cold).
So create a plan for cutting your `Pizza` using a variety of `Slice` arrangements.

Once you have an initial two `Pizza` breed them to make `Pizza` children.
To do this we can find a middle point on both `Pizza` (note we can have one with 5 `Slice` and another with 7 `Slice`).
This will be our Cross Point to swap the `Slice` in one with the `Slice` in another after this point.

See `HashCodeSolver.breed()`.

Ensure that your children aren't 50% pappa `Pizza` and 50% mamma `Pizza` by mutating their genes.
We can do this by swapping out random slices with any of the other valid `Slice` for us to maybe (just) be a fitter `Pizza`.
We can also remove one or add another extra `Slice` in if we have not explored all of the pizza space.

See `HashCodeSolver.mutate()`

So now we have the baby `Pizza` out of this generation which are like their parents but not 100%.
We have evolved our `Pizza` to be better or worse than our mamma or pappa `Pizza`.
Now this is where the sad part comes. Only the fittest two can survive the `Pizza` HashCode Hunger Games.
To ensure we're betting on the top two, lets compare our baby `Pizza` using the `fitness()` to establish who our chips should be on.
The Hunger Games is over and Peeta & Katniss `Pizza` remain.

Our baby `Pizza` have all grown up and now we can repeat the `breed()` and `mutate()` until we reach our best possible arrangement.

## Hurray, Pizza!

This isn't 100% ideal at calculating the best arrangement (Java = Slow, soz) and is merely showing you how you can solve represent it.
People have tried to solve it by looping left to right, top to bottom, vice versa to place large through to small untill all possible positions are filled.
This would be the most ideal way of going about it but that's just boring.