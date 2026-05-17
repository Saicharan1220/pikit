package com.mealorder.model;

/**
 * A single line item within an order — one menu item × a quantity.
 *
 * <p>Captures {@code unitPrice} at order time so price changes later
 * don't retroactively alter historical order totals. This is standard
 * e-commerce practice (and a classic interview gotcha question).
 */
public final class OrderItem {

    private final String menuItemId;
    private final String name;
    private final int    quantity;
    private final double unitPrice;  // snapshot at order time

    public OrderItem(String menuItemId, String name, int quantity, double unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        this.menuItemId = menuItemId;
        this.name       = name;
        this.quantity   = quantity;
        this.unitPrice  = unitPrice;
    }

    public String getMenuItemId() { return menuItemId; }
    public String getName()       { return name;       }
    public int    getQuantity()   { return quantity;   }
    public double getUnitPrice()  { return unitPrice;  }
    public double getSubtotal()   { return unitPrice * quantity; }

    @Override
    public String toString() {
        return String.format("OrderItem{%s x%d @ ₹%.2f}", name, quantity, unitPrice);
    }
}
