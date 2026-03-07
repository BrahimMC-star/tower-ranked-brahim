package zwuiix.colria.util;

import cn.nukkit.item.Item;
import zwuiix.colria.inventory.VirtualInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Window {
    private static final int TOP_RESERVED_ROWS = 2;
    private static final int BOTTOM_RESERVED_ROWS = 1;
    private static final int SIDE_MARGIN = 1;

    public static void fillBorder(VirtualInventory inv, Item item) {
        int size = inv.getSize();
        int cols = 9, rows = size / cols;

        for (int c = 0; c < cols; c++) {
            inv.setItem(c, item.clone());
            inv.setItem(size - cols + c, item.clone());
        }

        for (int r = 1; r < rows - 1; r++) {
            inv.setItem(r * cols, item.clone());
            inv.setItem(r * cols + cols - 1, item.clone());
        }
    }

    public static void fillSecondLine(VirtualInventory inv, Item item) {
        for(int c : new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17}) {
            inv.setItem(c, item.clone());
        }
    }

    public static void fillVerticalLine(int slot, VirtualInventory inv, Item item) {
        int size = inv.getSize();
        if (slot < 0 || slot >= size) return;

        final int cols = 9;
        final int rows = (size + cols - 1) / cols;
        final int col = slot % cols;
        int startRow = slot / cols;

        for (int r = startRow; r < rows; r++) {
            int index = r * cols + col;
            if (index >= size) break;
            inv.setItem(index, item.clone());
        }
    }

    public static void fillHorizontalLine(int slot, VirtualInventory inv, Item item) {
        int size = inv.getSize();
        if (slot < 0 || slot >= size) return;

        final int cols = 9;
        int rowStart = (slot / cols) * cols;
        int rowEndExclusive = Math.min(rowStart + cols, size);

        for (int i = slot; i < rowEndExclusive; i++) {
            inv.setItem(i, item.clone());
        }
    }

    public static int[] computeContentSlots(int invSize, int topRows, int bottomRows, int sideMargin) {
        if (invSize % 9 != 0) throw new IllegalArgumentException("Invalid inventory size: " + invSize);
        int rows = invSize / 9;
        int startRow = topRows;
        int endRow   = rows - 1 - bottomRows;
        int startCol = sideMargin;
        int endCol   = 8 - sideMargin;
        if (startRow > endRow || startCol > endCol) return new int[0];

        int count = (endRow - startRow + 1) * (endCol - startCol + 1);
        int[] slots = new int[count];
        int k = 0;
        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                slots[k++] = r * 9 + c;
            }
        }
        return slots;
    }

    public static int nextSlot(VirtualInventory inv) {
        int[] content = computeContentSlots(inv.getSize(), TOP_RESERVED_ROWS, BOTTOM_RESERVED_ROWS, SIDE_MARGIN);
        for (int slot : content) {
            Item item = inv.getItem(slot);
            if (item == null || item.isNull() || item.getCount() <= 0) {
                return slot;
            }
        }
        throw new NoSuchElementException("No free slots on the page.");
    }

    public static int nextSlot1(VirtualInventory inv) {
        int[] content = computeContentSlots(inv.getSize(), 1, BOTTOM_RESERVED_ROWS, SIDE_MARGIN);
        for (int slot : content) {
            Item item = inv.getItem(slot);
            if (item == null || item.isNull() || item.getCount() <= 0) {
                return slot;
            }
        }
        throw new NoSuchElementException("No free slots on the page.");
    }

    public static int remainingSlots(VirtualInventory inv) {
        int[] content = computeContentSlots(inv.getSize(), TOP_RESERVED_ROWS, BOTTOM_RESERVED_ROWS, SIDE_MARGIN);
        int free = 0;
        for (int slot : content) {
            Item item = inv.getItem(slot);
            if (item == null || item.isNull() || item.getCount() <= 0) free++;
        }
        return free;
    }

    public static List<Integer> freeSlots(VirtualInventory inv) {
        int[] content = computeContentSlots(inv.getSize(), TOP_RESERVED_ROWS, BOTTOM_RESERVED_ROWS, SIDE_MARGIN);
        ArrayList<Integer> out = new ArrayList<>();
        for (int slot : content) {
            Item item = inv.getItem(slot);
            if (item == null || item.isNull() || item.getCount() <= 0) out.add(slot);
        }
        return out;
    }

    public static void fill(VirtualInventory inventory, Item item) {
        int[] content = computeContentSlots(inventory.getSize(), TOP_RESERVED_ROWS, BOTTOM_RESERVED_ROWS, SIDE_MARGIN);
        for (int slot : content) {
            Item current = inventory.getItem(slot);
            if (current == null || current.isNull() || current.getCount() <= 0) {
                inventory.setItem(slot, item.clone());
            }
        }
    }
}
