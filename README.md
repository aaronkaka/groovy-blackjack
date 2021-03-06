# Groovy Blackjack
![Image](blackjack.jpg?raw=true "Blackjack! Groovy...")

My first Groovy script, a command-line Blackjack simulator.

## Requirements
Created and tested with Java 6 and Groovy 1.8. I did have to be UTF-8 compliant for Mac OS X Terminal.

If executing the game from the command line rather than an IDE, follow the directions for [installing Groovy](http://groovy.codehaus.org/Installing+Groovy)
and specify the directory for the card images:

    Blackjack/src> groovy blackjack -d "../img"

## Goals
Initially the goal was to create a simple Groovy command-line executable script to simulate casino-style Blackjack
purely with text characters. As with all such things, I found that in fact it was a little more complex than I hoped,
due to features like multiple decks, insurance, card graphics, splitting hands, a special rule when splitting Aces, etc.

So now I've created a Swing game panel that has graphical representation of the card hands for the dealer and player.
The input at this time is still CLI-driven, but that may evolve as well.

## Observations
When you are at a real casino, and you receive a 2 and Ace, the dealer will say "Three or Thirteen". This is only the
illusion of choice. Algorithmically speaking, only when you introduce enough card values to put you over 21 is the Ace
adjusted down to a value of 1.
