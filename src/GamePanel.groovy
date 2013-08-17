import javax.imageio.ImageIO
import javax.swing.*
import java.awt.*

class GamePanel extends JPanel {

    def dealerCards = []
    def dealer_x_coord = 35
    def dealer_y_coord = 50
    def dealer_x_title = dealer_x_coord + 30
    def dealer_y_title = dealer_y_coord - 10

    def playerCards = []
    def playerCardsSplit1 = []
    def playerCardsSplit2 = []
    def player_x_coord = 35
    def player_y_coord = 210
    def player_x_title = player_x_coord + 30
    def player_y_title = player_y_coord - 10

    def imagePath = "img"
    def fontName = "Times New Roman"
    def fontStyle = 1
    def fontSize = 16

    def regularOffset = 25
    def splitOffset = 100

    GamePanel(String imgPath = "") {
        if (imgPath != "") imagePath = imgPath
        this.setBackground(Color.green.darker().darker())
    }

    void updatePanel(String which, String imageFilename, boolean isFirstSplit = false, boolean isSecondSplit = false) {

        String imagePathString = imagePath + "/" + imageFilename

        if (which.equalsIgnoreCase("dealer")) {
            dealerCards.add(ImageIO.read(new File(imagePathString)))
        } else {
            if (isFirstSplit) {
                playerCardsSplit1.add(ImageIO.read(new File(imagePathString)))
            } else if (isSecondSplit) {
                playerCardsSplit2.add(ImageIO.read(new File(imagePathString)))
            } else {
                playerCards.add(ImageIO.read(new File(imagePathString)))
            }
        }

        repaint()
    }

    void dealHoleCard() {

        File f = new File(imagePath + "/" + "back-blue-75-2.png")

        if ( f.exists() ) {
            dealerCards.add(ImageIO.read(f))
        } else {
            println( "Missing hole card image at " + imagePath);
        }
    }

    void vanishHoleCard() {
        dealerCards.remove(0)
    }

    void resetXCoordForRepaint() {

        dealer_x_coord = 35
        player_x_coord = 35
    }

    void paint(Graphics g) {

        super.paintComponent(g)

        dealerCards.each {
            g.drawImage(it, dealer_x_coord, dealer_y_coord, null)
            dealer_x_coord += regularOffset
        }

        if (playerCardsSplit1) {
            playerCardsSplit1.each {
                g.drawImage(it, player_x_coord, player_y_coord, null)
                player_x_coord += regularOffset
            }
        }

        if (playerCardsSplit2) {

            player_x_coord += splitOffset

            playerCardsSplit2.each {
                g.drawImage(it, player_x_coord, player_y_coord, null)
                player_x_coord += regularOffset
            }
        }

        if (!playerCardsSplit1) {
            playerCards.each {
                g.drawImage(it, player_x_coord, player_y_coord, null)
                player_x_coord += regularOffset
            }
        }

        resetXCoordForRepaint()

        g.setColor(Color.white)
        g.setFont(new Font(fontName, fontStyle, fontSize))
        g.drawString("Dealer", dealer_x_title, dealer_y_title)
        g.drawString("Player", player_x_title, player_y_title)
    }

}