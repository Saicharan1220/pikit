package com.mealorder.model;

/**
 * Represents a single dish available in a restaurant's menu.
 *
 * <p>Immutable after creation — restaurants update menus by replacing MenuItems,
 * not mutating them. Immutability makes this safe to share across threads
 * without synchronization.
 */
public final class MenuItem {

    private final String   itemId;
    private final String   name;
    private final String   description;
    private final double   price;
    private final boolean  available;

    private MenuItem(Builder builder) {
        this.itemId      = builder.itemId;
        this.name        = builder.name;
        this.description = builder.description;
        this.price       = builder.price;
        this.available   = builder.available;
    }

    public String  getItemId()      { return itemId;      }
    public String  getName()        { return name;        }
    public String  getDescription() { return description; }
    public double  getPrice()       { return price;       }
    public boolean isAvailable()    { return available;   }

    @Override
    public String toString() {
        return String.format("MenuItem{%s, %s, ₹%.2f, available=%s}",
            itemId, name, price, available);
    }

    public static class Builder {
        private String  itemId;
        private String  name;
        private String  description = "";
        private double  price;
        private boolean available   = true;

        public Builder itemId(String v)      { this.itemId = v;      return this; }
        public Builder name(String v)        { this.name = v;        return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder price(double v)       { this.price = v;       return this; }
        public Builder available(boolean v)  { this.available = v;   return this; }

        public MenuItem build() {
            if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId required");
            if (name == null || name.isBlank())     throw new IllegalArgumentException("name required");
            if (price < 0)                          throw new IllegalArgumentException("price cannot be negative");
            return new MenuItem(this);
        }
    }
}
