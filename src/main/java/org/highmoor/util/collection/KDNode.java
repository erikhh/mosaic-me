package org.highmoor.util.collection;

import java.util.Vector;

// K-D Tree node class

class KDNode {

  // these are seen by KDTree
  protected HPoint key;

  Object value;

  protected KDNode left;
  protected KDNode right;

  protected boolean deleted;

  // Method ins translated from 352.ins.c of Gonnet & Baeza-Yates
  protected static KDNode ins(HPoint key, Object val, KDNode t, int lev, int k) {

    if (t == null) {
      t = new KDNode(key, val);
    } else if (key.equals(t.key)) {

      // "re-insert"
      if (t.deleted) {
        t.deleted = false;
        t.value = val;
      }

      // else {
      // throw new KeyDuplicateException();
      // }
    } else if (key.coord[lev] > t.key.coord[lev]) {
      t.right = ins(key, val, t.right, (lev + 1) % k, k);
    } else {
      t.left = ins(key, val, t.left, (lev + 1) % k, k);
    }

    return t;
  }

  // Method srch translated from 352.srch.c of Gonnet & Baeza-Yates
  protected static KDNode srch(HPoint key, KDNode t, int k) {

    for (int lev = 0; t != null; lev = (lev + 1) % k) {

      if (!t.deleted && key.equals(t.key)) {
        return t;
      } else if (key.coord[lev] > t.key.coord[lev]) {
        t = t.right;
      } else {
        t = t.left;
      }
    }

    return null;
  }

  // Method rsearch translated from 352.range.c of Gonnet & Baeza-Yates
  protected static void rsearch(HPoint lowk, HPoint uppk, KDNode t, int lev, int k, Vector<KDNode> v) {

    if (t == null) {
      return;
    }
    if (lowk.coord[lev] <= t.key.coord[lev]) {
      rsearch(lowk, uppk, t.left, (lev + 1) % k, k, v);
    }
    int j;
    for (j = 0; j < k && lowk.coord[j] <= t.key.coord[j] && uppk.coord[j] >= t.key.coord[j]; j++) {
      ;
    }
    if (j == k) {
      v.add(t);
    }
    if (uppk.coord[lev] > t.key.coord[lev]) {
      rsearch(lowk, uppk, t.right, (lev + 1) % k, k, v);
    }
  }

  // Method Nearest Neighbor from Andrew Moore's thesis. Numbered
  // comments are direct quotes from there. Step "SDL" is added to
  // make the algorithm work correctly. NearestNeighborList solution
  // courtesy of Bjoern Heckel.
  protected static void nnbr(KDNode kd, HPoint target, HRect hr, double maxDistSqd, int lev, int k, NearestNeighborList nnl) {

    // 1. if kd is empty then set dist-sqd to infinity and exit.
    if (kd == null) {
      return;
    }

    // 2. s := split field of kd
    int s = lev % k;

    // 3. pivot := dom-elt field of kd
    HPoint pivot = kd.key;

    // 4. Cut hr into to sub-hyperrectangles left-hr and right-hr.
    // The cut plane is through pivot and perpendicular to the s
    // dimension.
    HRect leftHr = hr; // optimize by not cloning
    HRect rightHr = (HRect) hr.clone();
    leftHr.max.coord[s] = pivot.coord[s];
    rightHr.min.coord[s] = pivot.coord[s];

    // 5. target-in-left := target_s <= pivot_s
    boolean targetInLeft = target.coord[s] < pivot.coord[s];

    KDNode nearerKd;
    HRect nearerHr;
    KDNode furtherKd;
    HRect furtherHr;

    // 6. if target-in-left then
    // 6.1. nearer-kd := left field of kd and nearer-hr := left-hr
    // 6.2. further-kd := right field of kd and further-hr := right-hr
    if (targetInLeft) {
      nearerKd = kd.left;
      nearerHr = leftHr;
      furtherKd = kd.right;
      furtherHr = rightHr;
    } else {
      // 7. if not target-in-left then
      // 7.1. nearer-kd := right field of kd and nearer-hr := right-hr
      // 7.2. further-kd := left field of kd and further-hr := left-hr
      nearerKd = kd.right;
      nearerHr = rightHr;
      furtherKd = kd.left;
      furtherHr = leftHr;
    }

    // 8. Recursively call Nearest Neighbor with paramters
    // (nearer-kd, target, nearer-hr, max-dist-sqd), storing the
    // results in nearest and dist-sqd
    nnbr(nearerKd, target, nearerHr, maxDistSqd, lev + 1, k, nnl);

    //KDNode nearest = (KDNode) nnl.getHighest();
    double distSqd;

    if (!nnl.isCapacityReached()) {
      distSqd = Double.MAX_VALUE;
    } else {
      distSqd = nnl.getMaxPriority();
    }

    // 9. max-dist-sqd := minimum of max-dist-sqd and dist-sqd
    maxDistSqd = Math.min(maxDistSqd, distSqd);

    // 10. A nearer point could only lie in further-kd if there were some
    // part of further-hr within distance sqrt(max-dist-sqd) of
    // target. If this is the case then
    HPoint closest = furtherHr.closest(target);
    double pivotToTarget = HPoint.sqrdist(pivot, target);
    if (HPoint.eucdist(closest, target) < Math.sqrt(maxDistSqd)) {

      // 10.1 if (pivot-target)^2 < dist-sqd then
      if (pivotToTarget < distSqd) {

        // 10.1.1 nearest := (pivot, range-elt field of kd)
        //nearest = kd;

        // 10.1.2 dist-sqd = (pivot-target)^2
        distSqd = pivotToTarget;

        // add to nnl
        if (!kd.deleted) {
          nnl.insert(kd, distSqd);
        }

        // 10.1.3 max-dist-sqd = dist-sqd
        // max_dist_sqd = dist_sqd;
        if (nnl.isCapacityReached()) {
          maxDistSqd = nnl.getMaxPriority();
        } else {
          maxDistSqd = Double.MAX_VALUE;
        }
      }

      // 10.2 Recursively call Nearest Neighbor with parameters
      // (further-kd, target, further-hr, max-dist_sqd),
      // storing results in temp-nearest and temp-dist-sqd
      nnbr(furtherKd, target, furtherHr, maxDistSqd, lev + 1, k, nnl);
      // KDNode tempNearest = (KDNode) nnl.getHighest();
      double tempDistSqd = nnl.getMaxPriority();

      // 10.3 If tmp-dist-sqd < dist-sqd then
      if (tempDistSqd < distSqd) {

        // 10.3.1 nearest := temp_nearest and dist_sqd := temp_dist_sqd
        //nearest = tempNearest;
        distSqd = tempDistSqd;
      }
    } else if (pivotToTarget < maxDistSqd) {
      // SDL: otherwise, current point is nearest
      //nearest = kd;
      distSqd = pivotToTarget;
    }
  }

  // constructor is used only by class; other methods are static
  private KDNode(HPoint key, Object val) {

    this.key = key;
    this.value = val;
    this.left = null;
    this.right = null;
    this.deleted = false;
  }

  protected String toString(int depth) {
    String s = key + "  " + value + (deleted ? "*" : "");
    if (left != null) {
      s = s + "\n" + pad(depth) + "L " + left.toString(depth + 1);
    }
    if (right != null) {
      s = s + "\n" + pad(depth) + "R " + right.toString(depth + 1);
    }
    return s;
  }

  private static String pad(int n) {
    String s = "";
    for (int i = 0; i < n; ++i) {
      s += " ";
    }
    return s;
  }
}
