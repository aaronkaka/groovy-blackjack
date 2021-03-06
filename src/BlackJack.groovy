import javax.swing.JFrame
import java.text.NumberFormat

def devMode = false

def cl = new CliBuilder(usage: 'groovy Blackjack -d "../img"')
cl.d(argName:'imgDir', longOpt:'directory', args:1, required:true, 'Directory for images, REQUIRED')
def opt = cl.parse(args)
String imageDir = ""
if (opt) {
    imageDir = opt.d
}

NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US)
Random generator = new Random()
final int CUTCARD = generator.nextInt(7-5+1) + 5 // Produce a random integer between 5 and 7 inclusive
final int ACE = 11

println "Welcome to Double Deck Blackjack at Aaron's Casino!"
println "House Rules: Split once per hand, dealer stands on all 17s."

// CLOSURES
def shuffle = { numDecks, addDeckClosure ->

    def combinedDecks = new ArrayList()

    numDecks.times {
        addDeckClosure(combinedDecks)
    }

    Collections.shuffle(combinedDecks)

    if (!devMode) {
        assert combinedDecks.size == numDecks * 52
    }

    return combinedDecks
}

def dealerCanTakeCard = { numAces, numAdjusted, total ->
    // following check is for first two cards in dealer's hand being A,A
    if (numAces == 2 && numAdjusted == 0)
      return true
    // dealer stands on 17 or higher
    if (total < 17)
      return true

    return false
}

def showPair = { firstCard, secondCard ->

    // Use the following PrintStream for Unicode characters (card suits)
    PrintStream out = new PrintStream(System.out, true, "UTF-8")

    def padding = 10

    out.println "-".padRight(padding+1, '-') + "  " + "-".padRight(padding+1, '-')
    out.print "| " + firstCard.rank.padRight(padding-2) + "|  "
    out.println "| " + secondCard.rank.padRight(padding-2) + "|"
    out.print "| " + firstCard.suit.padRight(padding-2) + "|  "
    out.println "| " + secondCard.suit.padRight(padding-2) + "|"
    2.times {
        out.println "|".padRight(padding) + "|  " + "|".padRight(padding) + "|"
    }
    out.print "|" + firstCard.suit.padLeft(padding-2) + " |  "
    out.println "|" + secondCard.suit.padLeft(padding-2) + " |"
    out.print "|" + firstCard.rank.padLeft(padding-2) + " |  "
    out.println "|" + secondCard.rank.padLeft(padding-2) + " |"
    out.println "-".padRight(padding+1, '-') + "  " + "-".padRight(padding+1, '-')
}

def processSplitHand = { splitHand, dealerTotal, dealerHasBJ, playerStake, which ->

    println "Player's " + which + " split hand totals " + splitHand.playerTotal
    if (dealerTotal < 22 && splitHand.playerTotal < 22) {
        if (dealerTotal > splitHand.playerTotal) {
            println "Dealer wins against " + which + " split hand"
            if (!dealerHasBJ)
                playerStake -= splitHand.playerBet
        }
        if (splitHand.playerTotal > dealerTotal) {
            println "Player wins " + which + " split hand!"
            if (splitHand.playerHasBJ)
                playerStake += splitHand.playerBet * 1.5
            else
                playerStake += splitHand.playerBet
        }
        if (splitHand.playerTotal == dealerTotal) {
            println "Push on " + which + " split hand..."
        }
    }

    return playerStake
}

def addDeck = { deck ->

    deck.add(new Card('A', Suit.CLUBS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.CLUBS, i))
    }
    deck.add(new Card('J', Suit.CLUBS, 10))
    deck.add(new Card('Q', Suit.CLUBS, 10))
    deck.add(new Card('K', Suit.CLUBS, 10))

    deck.add(new Card('A', Suit.SPADES, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.SPADES, i))
    }
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('Q', Suit.SPADES, 10))
    deck.add(new Card('K', Suit.SPADES, 10))


    deck.add(new Card('A', Suit.DIAMONDS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.DIAMONDS, i))
    }
    deck.add(new Card('J', Suit.DIAMONDS, 10))
    deck.add(new Card('Q', Suit.DIAMONDS, 10))
    deck.add(new Card('K', Suit.DIAMONDS, 10))


    deck.add(new Card('A', Suit.HEARTS, ACE))
    for (i in 2..10) {
        deck.add(new Card(i.toString(), Suit.HEARTS, i))
    }
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('Q', Suit.HEARTS, 10))
    deck.add(new Card('K', Suit.HEARTS, 10))

}

def testDeck = { deck ->
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.DIAMONDS, 10))
    deck.add(new Card('J', Suit.CLUBS, 10))
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('J', Suit.DIAMONDS, 10))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.CLUBS, 10))
    deck.add(new Card('J', Suit.SPADES, 10))
    deck.add(new Card('J', Suit.HEARTS, 10))
    deck.add(new Card('J', Suit.DIAMONDS, 10))
}

def leaveTable = { stake ->
    println "Player walks away with ${nf.format(stake)}\n"
    System.exit(0)
}
// END CLOSURES

def shoe
def numOfDecks = 2
def playerStake = 300
boolean timeToShuffle = false
// NEWHAND bootstraps on an existing shuffled shoe
if (devMode) {
    println "CUTCARD is " + CUTCARD.toString()
    shoe = shuffle(numOfDecks, testDeck)
} else {
    shoe = shuffle(numOfDecks, addDeck)
}

NEWHAND: while (shoe.size() > 0) {

    if (playerStake <= 0) {
        leaveTable(playerStake)
    }

    if (shoe.size() <= CUTCARD) {
        timeToShuffle = true
    }

    if (devMode) {
        println "***************DEV MODE****************"
        if (timeToShuffle) {
            println "Shuffling Test Deck..."
            shoe = shuffle(numOfDecks, testDeck)
            timeToShuffle = false
        }
    } else {
        println "***************************************"
        if (timeToShuffle) {
            println "Shuffling Deck..."
            shoe = shuffle(numOfDecks, addDeck)
            timeToShuffle = false
        }
    }

    print "Your stake is ${nf.format(playerStake)}. Enter bet or quit(q): "
    def betInput = new BufferedReader(new InputStreamReader(System.in)).readLine()

    def gamePanel = new GamePanel(imageDir)

    JFrame jframe = new JFrame()
    jframe.getContentPane().add(gamePanel)
    jframe.setSize(370,370)
    jframe.setTitle("Aaron's Casino Blackjack")
    jframe.setVisible(true)
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    try {
        playerBet = Integer.parseInt(betInput)
        
        if (playerBet == 0) {
            leaveTable(playerStake)
        }
        playerBet = playerBet.abs()
        if (playerBet > playerStake)
        {
            println "Bet of ${nf.format(playerBet)} exceeds Player Stake"
            continue NEWHAND
        }
    }
    catch (Exception e) {
        leaveTable(playerStake)
    }

    dealerTotal = 0
    numDealerAces = 0
    dealerAcesAdjusted = 0

    playerTotal = 0
    numPlayerAces = 0
    playerAcesAdjusted = 0

    card1 = shoe.remove(0)
    card2 = shoe.remove(0)
    card3 = shoe.remove(0)
    card4 = shoe.remove(0)

    println "Dealer is showing: "
    gamePanel.dealHoleCard()
    card4.show("dealer", gamePanel)

    println "You have: "
    showPair(card1, card3)
    gamePanel.updatePanel("player", card1.imageFilename)
    Thread.sleep(500)
    gamePanel.updatePanel("player", card3.imageFilename)

    if (card4.rank == 'A') {
        print "Do you want insurance (y/n)? "
        def insuranceInput = new BufferedReader(new InputStreamReader(System.in)).readLine()
        if (insuranceInput.equalsIgnoreCase("Y")) {
            if (playerStake >= playerBet/2) {
                if (card2.value == 10) {
                    playerStake += playerBet
                    println "Player won insurance bet!"
                } else {
                    playerStake -= playerBet/2
                    println "Player lost insurance bet - stake is now ${nf.format(playerStake)}"
                }
            } else
                println "Sorry, you don't have enough money to insure your bet"
        }
    }

    dealerTotal = card2.value + card4.value
    if (card2.rank == 'A') numDealerAces++
    if (card4.rank == 'A') numDealerAces++

    playerTotal = card1.value + card3.value
    if (card1.rank == 'A') numPlayerAces++
    if (card3.rank == 'A') numPlayerAces++

    dealerHasBJ = false
    if (dealerTotal == 21) {
        println "Dealer has blackjack!"
        dealerHasBJ = true
    }

    playerHasBJ = false
    if (playerTotal == 21) {
        println "Player has blackjack!"
        playerHasBJ = true
    }

    if (dealerHasBJ && playerHasBJ)
      println "What are the odds!"
    else if (playerHasBJ)
      playerStake += playerBet * 1.5
    else if (dealerHasBJ)
      playerStake -= playerBet

    def playerHand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, false, false)
    def split1Hand = null
    def split2Hand = null
    def isSplit = false
    
    if (!dealerHasBJ && !playerHasBJ) {

        if (card1.rank.equals(card3.rank)) {
            print "Do you wish to split (y/n)? "
            def splitInput = new BufferedReader(new InputStreamReader(System.in)).readLine()
            if (splitInput.equalsIgnoreCase("Y")) isSplit = true
        }

        if (!isSplit) {

            playerHand.playOut(gamePanel)
            playerStake = playerHand.playerStake // account for player bust
            playerBet = playerHand.playerBet // account for double down bet and not busted
            playerTotal = playerHand.playerTotal
            
        } else {
            splittingAces = false
            if (card1.rank == 'A') splittingAces = true

            split1 = shoe.remove(0)
            split2 = shoe.remove(0)

            println "\nFirst split hand:"
            showPair(card1, split1)
            gamePanel.updatePanel("player", card1.imageFilename, true, false)
            gamePanel.updatePanel("player", split1.imageFilename, true, false)

            playerTotal = card1.value + split1.value
            numPlayerAces = 0
            playerAcesAdjusted = 0
            if (card1.rank == 'A') numPlayerAces++
            if (split1.rank == 'A') numPlayerAces++
            
            split1PlayerHasBJ = false
            if (playerTotal == 21 && !splittingAces) {
                println "Player has Blackjack on first split hand!"
                split1PlayerHasBJ = true
            }
            split1Hand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, split1PlayerHasBJ, splittingAces)
            split1Hand.playOut(gamePanel, true, false)
            playerStake = split1Hand.playerStake // account for player bust on hit/double

            println "\nSecond split hand:"
            showPair(card3, split2)
            gamePanel.updatePanel("player", card3.imageFilename, false, true)
            gamePanel.updatePanel("player", split2.imageFilename, false, true)

            playerTotal = card3.value + split2.value
            numPlayerAces = 0
            playerAcesAdjusted = 0
            if (card3.rank == 'A') numPlayerAces++
            if (split2.rank == 'A') numPlayerAces++
            
            split2PlayerHasBJ = false
            if (playerTotal == 21 && !splittingAces) {
                println "Player has Blackjack on second split hand!"
                split2PlayerHasBJ = true
            }
            split2Hand = new PlayerHand(shoe, CUTCARD, playerBet, playerStake, playerTotal, numPlayerAces, playerAcesAdjusted, split2PlayerHasBJ, splittingAces)
            split2Hand.playOut(gamePanel, false, true)
            playerStake = split2Hand.playerStake // account for player bust on hit/double
        }

        DEALERHAND: while (shoe.size() > CUTCARD) {

            if ( (!isSplit && playerTotal < 22 && dealerCanTakeCard(numDealerAces, dealerAcesAdjusted, dealerTotal)) ||
                (isSplit && !(split1Hand.playerTotal > 21 && split2Hand.playerTotal > 21) && dealerCanTakeCard(numDealerAces, dealerAcesAdjusted, dealerTotal)) ) {

                def nextCard = shoe.remove(0)
                println "Dealer receives:"
                nextCard.show("dealer", gamePanel)

                if (nextCard.rank == 'A') numDealerAces++

                dealerTotal += nextCard.value

                if (dealerTotal > 21) {

                    // Check Aces and adjust as necessary
                    if (numDealerAces > 0) {
                        for (i=0; i < numDealerAces-dealerAcesAdjusted; i++) {
                          dealerTotal -= 10
                          dealerAcesAdjusted++
                          if (dealerTotal < 22) break // for loop
                        }
                    }
                    if (dealerTotal > 21) {
                        println "Dealer busted!"
                        if (!isSplit) {
                            playerStake += playerBet
                        } else {
                            // Only one or none of split hands busted.
                            // If hand did bust, playerStake already accounted.
                            if (split1Hand.playerTotal < 22)
                                playerStake += split1Hand.playerBet
                            if (split2Hand.playerTotal < 22)
                                playerStake += split2Hand.playerBet
                        }
                        break DEALERHAND
                    }
                }

          } else break DEALERHAND
        } // end DEALERHAND

    } // end No one has BJ

    Thread.sleep(2000)
    println "Dealer's other card was:"
    gamePanel.vanishHoleCard()
    card2.show("dealer", gamePanel)

    if (!isSplit) {
        println "Dealer totals " + dealerTotal + " and player totals " + playerTotal
        if (dealerTotal < 22 && playerTotal < 22) {
            if (dealerTotal > playerTotal) {
                println "Dealer wins"
                if (!dealerHasBJ)
                  playerStake -= playerBet
            }
            if (playerTotal > dealerTotal) {
                println "Player wins!"
                if (!playerHasBJ)
                  playerStake += playerBet
            }
            if (playerTotal == dealerTotal) {
                println "Push..."
            }
        }
    } else {
        
        println "Dealer totals " + dealerTotal

        playerStake = processSplitHand(split1Hand, dealerTotal, dealerHasBJ, playerStake, "first")

        playerStake = processSplitHand(split2Hand, dealerTotal, dealerHasBJ, playerStake, "second")
    }

} // end NEWHAND