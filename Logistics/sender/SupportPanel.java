package sender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SupportPanel extends JPanel {
    private JPanel faqListContainer;
    private JEditorPane answerDisplay;
    private List<FAQItem> allFaqItems;
    private JPanel selectedPanel = null;

    // Professional Logistics Color Palette
    private final Color SIDEBAR_BG = new Color(241, 245, 249);
    private final Color CONTENT_BG = Color.WHITE;
    private final Color TEXT_MAIN = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(71, 85, 105);
    private final Color BORDER_COLOR = new Color(203, 213, 225);

    private static class FAQItem {
        String icon;
        String question;
        String htmlAnswer;

        FAQItem(String icon, String question, String htmlAnswer) {
            this.icon = icon;
            this.question = question;
            this.htmlAnswer = htmlAnswer;
        }
    }

    public SupportPanel() {
        this.allFaqItems = new ArrayList<>();
        initializeComprehensiveFAQ();
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        // --- LEFT SIDEBAR (SCROLLABLE CATEGORIES) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(420, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel sidebarHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        sidebarHeader.setBackground(SIDEBAR_BG);
        sidebarHeader.setBorder(new EmptyBorder(35, 25, 25, 25));
        
        JLabel sideTitle = new JLabel("LogiXpress Help Center");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sideTitle.setForeground(TEXT_MAIN);
        
        JLabel sideSub = new JLabel("Browse our comprehensive guides");
        sideSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sideSub.setForeground(TEXT_MUTED);
        
        sidebarHeader.add(sideTitle);
        sidebarHeader.add(sideSub);

        faqListContainer = new JPanel();
        faqListContainer.setLayout(new BoxLayout(faqListContainer, BoxLayout.Y_AXIS));
        faqListContainer.setBackground(SIDEBAR_BG);
        
        renderFAQList();

        JScrollPane sideScroll = new JScrollPane(faqListContainer);
        sideScroll.setBorder(null);
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);
        sideScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sidebar.add(sidebarHeader, BorderLayout.NORTH);
        sidebar.add(sideScroll, BorderLayout.CENTER);

        // --- RIGHT CONTENT VIEW ---
        answerDisplay = new JEditorPane();
        answerDisplay.setEditable(false);
        answerDisplay.setContentType("text/html");
        answerDisplay.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setWelcomeHTML();

        JScrollPane contentScroll = new JScrollPane(answerDisplay);
        contentScroll.setBorder(null);
        contentScroll.getVerticalScrollBar().setUnitIncrement(20);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(sidebar, BorderLayout.WEST);
        add(contentScroll, BorderLayout.CENTER);
    }

    private void renderFAQList() {
        for (FAQItem item : allFaqItems) {
            faqListContainer.add(createFAQRow(item));
        }
    }

    private JPanel createFAQRow(FAQItem item) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setBackground(SIDEBAR_BG);
        row.setBorder(new EmptyBorder(20, 25, 20, 25));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel("<html><body style='width: 340px;'>" + item.icon + " &nbsp; " + item.question + "</body></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(30, 41, 59));
        
        row.add(label, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectedPanel != null) {
                    selectedPanel.setBackground(SIDEBAR_BG);
                    ((JLabel)selectedPanel.getComponent(0)).setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
                selectedPanel = row;
                row.setBackground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                displayAnswer(item);
            }
            public void mouseEntered(MouseEvent e) {
                if (row != selectedPanel) row.setBackground(new Color(226, 232, 240));
            }
            public void mouseExited(MouseEvent e) {
                if (row != selectedPanel) row.setBackground(SIDEBAR_BG);
            }
        });

        return row;
    }

    private void displayAnswer(FAQItem item) {
        String html = "<html><body style='font-family:Segoe UI; color:#0f172a; padding:50px;'>"
                + "<p style='color:#3b82f6; font-weight:bold; letter-spacing:1px; margin-bottom:5px;'>OFFICIAL GUIDE</p>"
                + "<h1 style='font-size:32px; margin-top:0; color:#1e3a8a;'>" + item.icon + " " + item.question + "</h1>"
                + "<hr style='border:0; border-top:2px solid #f1f5f9; margin: 30px 0;'>"
                + "<div style='font-size:11.5pt; line-height:1.7;'>"
                + item.htmlAnswer
                + "</div>"
                + "</body></html>";
        answerDisplay.setText(html);
        answerDisplay.setCaretPosition(0);
    }

    private void setWelcomeHTML() {
        answerDisplay.setText("<html><body style='text-align:center; padding-top:150px; font-family:Segoe UI;'>"
                + "<div style='color:#e2e8f0; font-size:80px;'>📋</div>"
                + "<h2 style='color:#1e293b; margin-top:20px;'>How can we help?</h2>"
                + "<p style='color:#64748b;'>Select a topic on the left for detailed logistics support,<br>policy information, and troubleshooting steps.</p>"
                + "</body></html>");
    }

    private void initializeComprehensiveFAQ() {
        // 1. Tracking
        allFaqItems.add(new FAQItem("📍", "Advanced Tracking & Notifications", 
            "LogiXpress utilizes a state-of-the-art Global Positioning System to provide real-time visibility. Every package is scanned at a minimum of 6 checkpoints.<br><br>" +
            "<b>Notification Options:</b><br>" +
            "• <b>SMS Alerts:</b> Receive a message when the driver is within 2km of your address.<br>" +
            "• <b>Email Reports:</b> A daily summary of all your active shipments and their ETAs.<br>" +
            "• <b>Webhooks:</b> For business users, we offer API endpoints to push tracking data directly to your internal systems.<br><br>" +
            "<i>Note: 'Held at Customs' status typically resolves in 24-48 hours once documentation is verified.</i>"));

        // 2. Shipping Methods
        allFaqItems.add(new FAQItem("🚀", "Shipping Methods & Timeframes", 
            "We categorize our logistics into three main workflows to balance cost and speed:<br><br>" +
            "<table border='0' width='100%' cellpadding='10' style='background:#f8fafc;'>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Saver (Sea/Road)</b></td><td style='border-bottom:1px solid #e2e8f0;'>7-14 Days</td><td style='border-bottom:1px solid #e2e8f0;'>Most economical for heavy items.</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Standard (Air)</b></td><td style='border-bottom:1px solid #e2e8f0;'>3-5 Days</td><td style='border-bottom:1px solid #e2e8f0;'>Default choice for small to medium parcels.</td></tr>" +
            "<tr><td><b>Next-Flight-Out</b></td><td>24 Hours</td><td>Urgent medical or legal document priority.</td></tr>" +
            "</table><br>" +
            "<b>Cut-off Times:</b> Orders confirmed before 2:00 PM (Local Time) will be picked up on the same day."));

        // 3. Claims
        allFaqItems.add(new FAQItem("📋", "Claims, Damages & Compensation", 
            "Our claims process is designed to be fair and transparent. If your package arrives damaged or is confirmed lost, please follow these steps:<br><br>" +
            "<b>1. Documentation:</b> Capture high-resolution photos of the external box, the internal packing material, and the damaged item itself.<br>" +
            "<b>2. Submission:</b> Open a ticket in this panel under 'File a Claim'. You will need your Order ID and Invoice.<br>" +
            "<b>3. Inspection:</b> For items valued over RM 500, a virtual inspection may be required with one of our agents.<br><br>" +
            "<div style='background:#fff1f2; padding:15px; border-radius:5px;'>" +
            "<b style='color:#be123c;'>Important:</b> Do not discard the packaging until the claim is resolved, as we may need to retrieve it for forensic investigation." +
            "</div>"));

        // 4. Packaging
        allFaqItems.add(new FAQItem("📦", "Professional Packaging Standards", 
            "To prevent damage and ensure the safety of our couriers, we enforce strict packaging guidelines:<br><br>" +
            "• <b>The H-Tape Method:</b> All seams of the box must be taped in an 'H' pattern to prevent accidental opening.<br>" +
            "• <b>Internal Clearance:</b> Maintain at least 5cm of cushioning (bubble wrap or foam) between the item and the box walls.<br>" +
            "• <b>Weight Limits:</b> Individual boxes must not exceed 30kg. For heavier items, please use a palletized shipping method.<br><br>" +
            "Items shipped in original manufacturer retail boxes (e.g., a TV box) without an outer shipping carton are shipped at the 'Owner's Risk'."));

        // 5. International
        allFaqItems.add(new FAQItem("🌍", "International Shipping & Customs", 
            "LogiXpress operates in 50+ countries. Shipping internationally requires specific documentation to clear customs efficiently.<br><br>" +
            "<b>Required Documents:</b><br>" +
            "1. Commercial Invoice (3 copies).<br>" +
            "2. Packing List detailing every item's material and origin.<br>" +
            "3. HS Codes (Harmonized System) to determine tax rates.<br><br>" +
            "<b>Duties & Taxes:</b> By default, shipments are sent <b>DDU (Delivered Duty Unpaid)</b>. This means the receiver is responsible for paying local import VAT and customs fees before the package is released."));

        // 6. Prohibited
        allFaqItems.add(new FAQItem("🚫", "Prohibited Items List", 
            "For the safety of our aircraft and ground crew, the following items are strictly forbidden:<br><br>" +
            "• <b>Dangerous Goods:</b> Aerosols, perfumes, lithium batteries (uninstalled), and magnets.<br>" +
            "• <b>Legal Restrictions:</b> Counterfeit goods, currency, and tobacco products.<br>" +
            "• <b>Perishables:</b> Fresh meat, fruit, or items requiring constant refrigeration.<br><br>" +
            "Attempting to ship prohibited items may result in the permanent suspension of your LogiXpress account and potential legal action by civil aviation authorities."));

        // 7. Account
        allFaqItems.add(new FAQItem("👤", "Account & Privacy Security", 
            "We take your data security seriously. Your account utilizes 256-bit AES encryption for all personal and financial data.<br><br>" +
            "<b>Security Recommendations:</b><br>" +
            "• Enable Two-Factor Authentication (2FA) in your profile settings.<br>" +
            "• Regularly audit your 'Saved Addresses' to ensure delivery accuracy.<br>" +
            "• LogiXpress staff will <b>never</b> ask for your password via phone or email.<br><br>" +
            "If you suspect your account has been compromised, use the 'Lock Account' feature immediately and contact our security team."));
            
        // 8. Returns
        allFaqItems.add(new FAQItem("🔄", "Returns & Reverse Logistics", 
            "E-commerce businesses can leverage our 'Easy-Return' system to manage customer exchanges seamlessly.<br><br>" +
            "<b>How it works:</b><br>" +
            "1. Generate a Return Label in your dashboard.<br>" +
            "2. Send the PDF label to your customer.<br>" +
            "3. Once scanned by our courier, the return status updates in your 'Incoming' tab.<br><br>" +
            "Returns are billed at a flat rate based on the original zone distance."));

        // 9. Lost Package
        allFaqItems.add(new FAQItem("🔍", "What to do if my package is lost?", 
            "If your package has not been updated for more than 7 business days, please follow these steps:<br><br>" +
            "<b>Step 1:</b> Verify the tracking status on our website. Refresh and check if there are any 'Exception' events.<br>" +
            "<b>Step 2:</b> Contact our customer support with your tracking number and order invoice.<br>" +
            "<b>Step 3:</b> We will initiate a 'Lost Package Investigation' which takes 3-5 business days.<br>" +
            "<b>Step 4:</b> If confirmed lost, we will process your compensation based on declared value (up to RM 500 for standard shipping).<br><br>" +
            "<div style='background:#e0f2fe; padding:15px; border-radius:5px;'>" +
            "<b style='color:#0369a1;'>Pro Tip:</b> For high-value items, always purchase our 'Enhanced Protection Plan' at checkout for full coverage up to RM 10,000." +
            "</div>"));

        // 10. Reschedule Delivery
        allFaqItems.add(new FAQItem("📅", "Reschedule Delivery or Change Address", 
            "Need to change where or when your package arrives? We offer flexible options:<br><br>" +
            "<b>Reschedule Delivery:</b><br>" +
            "• Go to 'Track Shipment' > 'Manage Delivery' > Select a new date (must be within 7 days).<br>" +
            "• Fee: Free for the first reschedule, RM 10 for subsequent changes.<br><br>" +
            "<b>Change Address:</b><br>" +
            "• Available only before the 'Out for Delivery' status.<br>" +
            "• Address changes within the same city: RM 15.<br>" +
            "• Address changes to a different city: RM 35 + additional transit time.<br><br>" +
            "<i>Note: Changes cannot be made once the driver has marked 'Attempted Delivery'.</i>"));

        // 11. Business Account
        allFaqItems.add(new FAQItem("🏢", "Business Account & Bulk Shipping", 
            "Upgrade to a LogiXpress Business Account to unlock premium features:<br><br>" +
            "<b>Business Benefits:</b><br>" +
            "• <b>Volume Discounts:</b> Up to 40% off standard rates for 500+ shipments/month.<br>" +
            "• <b>Dedicated Account Manager:</b> Priority support and proactive issue resolution.<br>" +
            "• <b>Bulk Upload:</b> Upload CSV files to create 1000+ shipping labels at once.<br>" +
            "• <b>Branded Tracking Page:</b> Customize the tracking page with your company logo and colors.<br>" +
            "• <b>Monthly Invoicing:</b> Net-30 payment terms instead of prepay.<br><br>" +
            "<a href='#' style='color:#3b82f6;'>Contact our sales team</a> for a customized quote."));

        // 12. Insurance
        allFaqItems.add(new FAQItem("🛡️", "Shipping Insurance Options", 
            "Protect your valuable shipments with our comprehensive insurance coverage:<br><br>" +
            "<b>Standard Coverage (Included Free):</b><br>" +
            "• Up to RM 100 for lost packages<br>" +
            "• Up to RM 50 for damaged items<br>" +
            "• Excludes electronics, jewelry, and artwork<br><br>" +
            "<b>Enhanced Protection Plan (Purchase Option):</b><br>" +
            "• Cost: 3% of declared value (minimum RM 5)<br>" +
            "• Coverage up to RM 10,000 per shipment<br>" +
            "• Includes theft, damage, and loss<br>" +
            "• Expedited claims processing (3-5 business days)<br><br>" +
            "<b>Premium Plan (Business Only):</b><br>" +
            "• Coverage up to RM 50,000<br>" +
            "• Includes consequential damages<br>" +
            "• 24-hour claims approval guarantee"));

        // 13. Pickup Request
        allFaqItems.add(new FAQItem("🚚", "Schedule a Package Pickup", 
            "No need to drop off your packages! Request a free pickup from your location:<br><br>" +
            "<b>How to schedule:</b><br>" +
            "1. Go to 'Shipments' > 'Request Pickup'<br>" +
            "2. Enter the number of packages and total weight<br>" +
            "3. Select a date and time window (9AM-12PM, 12PM-3PM, 3PM-6PM)<br>" +
            "4. Print your shipping labels and attach them to packages<br>" +
            "5. Our driver will scan and collect during the selected window<br><br>" +
            "<b>Requirements:</b><br>" +
            "• All packages must have labels attached<br>" +
            "• Total weight under 50kg per pickup (free)<br>" +
            "• For 50-200kg, a nominal fee of RM 20 applies<br>" +
            "• For over 200kg, please contact our freight department"));

        // 14. Holiday Schedule
        allFaqItems.add(new FAQItem("🎄", "Holiday Operating Hours", 
            "LogiXpress remains operational during most holidays, with adjusted schedules:<br><br>" +
            "<b>Major Holidays (Limited Service):</b><br>" +
            "• Chinese New Year (2 days) - No pickup, delivery only to commercial addresses<br>" +
            "• Hari Raya Aidilfitri (2 days) - Limited delivery in major cities only<br>" +
            "• Deepavali (1 day) - No operations<br>" +
            "• Christmas Day (1 day) - No operations<br>" +
            "• National Day (1 day) - No operations for residential delivery<br><br>" +
            "<b>Peak Season (November - January):</b><br>" +
            "• Extended pickup hours until 8PM<br>" +
            "• Saturday delivery available (RM 15 surcharge)<br>" +
            "• Expect 1-2 additional transit days due to volume<br><br>" +
            "<i>Check our <a href='#' style='color:#3b82f6;'>Holiday Schedule Page</a> for specific dates each year.</i>"));

        // 15. Contact Support
        allFaqItems.add(new FAQItem("📞", "Contact Customer Support", 
            "Our support team is available 24/7 to assist you with any logistics needs:<br><br>" +
            "<b>Support Channels:</b><br>" +
            "• <b>Live Chat:</b> Available in the bottom-right corner of your dashboard (Instant response)<br>" +
            "• <b>Phone Support:</b> 1-800-88-LOGI (5644) - 24/7<br>" +
            "• <b>Email:</b> support@logixpress.com (Response within 2 hours)<br>" +
            "• <b>WhatsApp:</b> +60 12-345 6789 (9AM - 9PM daily)<br><br>" +
            "<b>For Urgent Issues:</b><br>" +
            "• Lost packages: Use 'Priority' option in live chat<br>" +
            "• Damaged goods: Email photos directly to claims@logixpress.com<br>" +
            "• Delivery emergencies: Call our hotline and press 3 for immediate assistance<br><br>" +
            "<b>Support Hours by Channel:</b><br>" +
            "• Live Chat: 24/7<br>" +
            "• Phone: 24/7<br>" +
            "• Email: 8AM - 10PM (Responses within 1 hour during business hours)<br>" +
            "• WhatsApp: 9AM - 9PM daily"));

        // 16. Tracking Status Explanation
        allFaqItems.add(new FAQItem("📊", "Understanding Tracking Statuses", 
            "Decode our tracking statuses to know exactly where your package is:<br><br>" +
            "<b>Common Statuses & Meanings:</b><br>" +
            "<table border='0' width='100%' cellpadding='8' style='background:#f8fafc;'>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Order Received</b></td><td style='border-bottom:1px solid #e2e8f0;'>We have your shipping information</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Picked Up</b></td><td style='border-bottom:1px solid #e2e8f0;'>Driver has collected the package</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>In Transit</b></td><td style='border-bottom:1px solid #e2e8f0;'>Package is moving through our network</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Out for Delivery</b></td><td style='border-bottom:1px solid #e2e8f0;'>Driver is on the way (ETA 2-6 hours)</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Delivered</b></td><td style='border-bottom:1px solid #e2e8f0;'>Package dropped off with proof of delivery</td></tr>" +
            "<tr><td style='border-bottom:1px solid #e2e8f0;'><b>Exception</b></td><td style='border-bottom:1px solid #e2e8f0;'>Issue detected - click for details</td></tr>" +
            "<tr><td><b>Return to Sender</b></td><td>Package is coming back to you</td></tr>" +
            "</table><br>" +
            "<b>Exception Types:</b><br>" +
            "• 'Address Issue' - Invalid or incomplete address<br>" +
            "• 'Customs Hold' - Awaiting documentation<br>" +
            "• 'Delivery Attempted' - No one was home<br>" +
            "• 'Weather Delay' - Service temporarily suspended"));

        // 17. Shipping Rates
        allFaqItems.add(new FAQItem("💰", "Shipping Rates & Fees", 
            "Calculate your shipping costs based on weight, dimensions, and destination:<br><br>" +
            "<b>Domestic Rates (Peninsular Malaysia):</b><br>" +
            "• Up to 1kg: RM 8<br>" +
            "• 1-3kg: RM 12<br>" +
            "• 3-5kg: RM 18<br>" +
            "• 5-10kg: RM 25<br>" +
            "• 10-20kg: RM 40<br>" +
            "• 20-30kg: RM 55<br><br>" +
            "<b>East Malaysia (Sabah/Sarawak):</b><br>" +
            "• Add RM 15-30 depending on weight<br><br>" +
            "<b>International (Per kg):</b><br>" +
            "• Singapore: RM 25/kg<br>" +
            "• Thailand: RM 30/kg<br>" +
            "• Indonesia: RM 35/kg<br>" +
            "• Vietnam/Philippines: RM 40/kg<br>" +
            "• China/HK/Taiwan: RM 45/kg<br>" +
            "• USA/Europe: RM 80/kg<br><br>" +
            "<b>Additional Fees:</b><br>" +
            "• Fuel surcharge: 5% of base rate<br>" +
            "• Insurance: 3% of declared value<br>" +
            "• Saturday delivery: RM 15<br>" +
            "• Address correction: RM 10"));

        // 18. Self-Service Portal
        allFaqItems.add(new FAQItem("💻", "Self-Service Portal Features", 
            "Manage all your shipping needs through our comprehensive self-service portal:<br><br>" +
            "<b>Available Actions:</b><br>" +
            "• <b>Create Shipment:</b> Generate single or bulk labels<br>" +
            "• <b>Track Packages:</b> Real-time updates for all shipments<br>" +
            "• <b>Schedule Pickup:</b> Request courier collection<br>" +
            "• <b>View History:</b> Access past 12 months of shipments<br>" +
            "• <b>Download Reports:</b> Export shipping data to CSV/Excel<br>" +
            "• <b>Manage Address Book:</b> Save frequently used addresses<br>" +
            "• <b>File Claims:</b> Submit damage or loss claims online<br>" +
            "• <b>Invoice & Payment:</b> View statements and make payments<br>" +
            "• <b>API Settings:</b> Generate API keys for integration<br><br>" +
            "<b>Mobile App Features:</b><br>" +
            "• Scan QR codes for instant tracking<br>" +
            "• Push notifications for status changes<br>" +
            "• One-click reorder of frequent shipments<br>" +
            "• Driver live map for incoming deliveries"));

        // 19. Customs Documentation
        allFaqItems.add(new FAQItem("📄", "Customs Documentation Guide", 
            "Avoid delays with proper customs paperwork for international shipments:<br><br>" +
            "<b>Required Documents for ALL International Shipments:</b><br>" +
            "1. <b>Commercial Invoice (3 copies)</b> - Must include:<br>" +
            "   • Seller and buyer complete details<br>" +
            "   • Quantity, unit value, total value of each item<br>" +
            "   • Country of origin for each product<br>" +
            "   • Reason for export (sale, gift, return, sample)<br>" +
            "2. <b>Packing List</b> - Detailed breakdown by package<br>" +
            "3. <b>Waybill/Air Waybill</b> - Our generated shipping label<br><br>" +
            "<b>Additional Documents (Specific Scenarios):</b><br>" +
            "• <b>Proforma Invoice</b> - For samples or unsold goods<br>" +
            "• <b>Certificate of Origin</b> - For preferential duty rates under FTA<br>" +
            "• <b>MSDS (Material Safety Data Sheet)</b> - For chemicals/batteries<br>" +
            "• <b>Import License</b> - For restricted items (food, pharmaceuticals)<br>" +
            "• <b>FDA Certificate</b> - For food/medical devices to USA<br><br>" +
            "<div style='background:#f0fdf4; padding:15px; border-radius:5px;'>" +
            "<b style='color:#15803d;'>Pro Tip:</b> Use our online customs tool to generate accurate commercial invoices automatically from your order details!" +
            "</div>"));

        // 20. Eco-Friendly Shipping
        allFaqItems.add(new FAQItem("🌱", "Eco-Friendly Shipping Options", 
            "LogiXpress is committed to reducing our carbon footprint. Join our green initiative:<br><br>" +
            "<b>Our Sustainability Commitments:</b><br>" +
            "• <b>Electric Delivery Fleet:</b> 30% of urban deliveries now use electric vans (Target: 100% by 2030)<br>" +
            "• <b>Carbon-Neutral Shipping:</b> Offset your shipment's carbon emissions for just RM 0.50<br>" +
            "• <b>Eco-Packaging:</b> 100% recycled and biodegradable boxes (available at RM 1 extra)<br>" +
            "• <b>Paperless Operations:</b> Digital waybills and electronic proof of delivery<br>" +
            "• <b>Route Optimization:</b> AI-driven routes reduce fuel consumption by 25%<br><br>" +
            "<b>How to participate:</b><br>" +
            "• Select 'Carbon Neutral' option when creating a shipment<br>" +
            "• Choose 'Eco-Packaging' in your packing preferences<br>" +
            "• Opt for 'No Rush Delivery' (adds 2-3 days) for consolidated shipping<br><br>" +
            "<b>Our Impact (2024):</b><br>" +
            "• 15,000 tons CO2 reduced<br>" +
            "• 500,000+ eco-packaging boxes used<br>" +
            "• 2 million digital documents saved"));
    }
}