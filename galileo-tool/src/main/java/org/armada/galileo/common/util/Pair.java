package org.armada.galileo.common.util;

import java.util.Objects;

/**
 * @author xiaobo
 * @date 2022/12/16 11:44
 */

public class Pair<L, R> {

    private L left;

    private R right;


    private Pair() {
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        Pair<L, R> pair = new Pair<>();
        pair.left = left;
        pair.right = right;
        return pair;
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Pair{");
        sb.append("left=").append(left);
        sb.append(", right=").append(right);
        sb.append('}');
        return sb.toString();
    }
}
