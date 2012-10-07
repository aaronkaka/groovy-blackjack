package src

import javax.swing.JPanel
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Font

class GamePanel extends JPanel {

    def dealerCards = []
    def dealer_x_coord = 35
    def dealer_y_coord = 50
    def dealer_x_title = dealer_x_coord + 30
    def dealer_y_title = dealer_y_coord - 10

    def playerCards = []
    def player_x_coord = 35
    def player_y_coord = 210
    def player_x_title = player_x_coord + 30
    def player_y_title = player_y_coord - 10

    def imagePath = "../img/"
    def regularOffset = 25
    def fontName = "Times New Roman"
    def fontStyle = 1
    def fontSize = 16

    public GamePanel() {

        this.setBackground(Color.green)

    }

    public void updatePanel() {

        dealerCards.add(ImageIO.read(new File(imagePath + "back-red-75-2.png")))
        dealerCards.add(ImageIO.read(new File(imagePath + "clubs-2-75.png")))

        playerCards.add(ImageIO.read(new File(imagePath + "back-red-75-2.png")))
        playerCards.add(ImageIO.read(new File(imagePath + "clubs-2-75.png")))

        repaint()
    }

    public void resetRegularHand(which) {

        if (which == 'dealer') {
            dealerCards.clear()
        } else {
            playerCards.clear()
        }
    }

    public void paint(Graphics g) {

        super.paintComponent(g)

        dealerCards.each {
            g.drawImage(it, dealer_x_coord, dealer_y_coord, null)
            dealer_x_coord += regularOffset
        }

        playerCards.each {
            g.drawImage(it, player_x_coord, player_y_coord, null)
            player_x_coord += regularOffset
        }

        g.setFont(new Font(fontName, fontStyle, fontSize))
        g.drawString("Dealer", dealer_x_title, dealer_y_title)
        g.drawString("Player", player_x_title, player_y_title)
    }

}