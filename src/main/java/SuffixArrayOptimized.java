/**
 * The SuffixArrayOptimized class represents a suffix array of a string, but
 * with less memory usage than the SuffixArray class.
 *
 * It supports the following operations: computing the length() of the text,
 * selectAsString() the ith smallest suffix, getting the indexOf() select(i),
 * the length of longestCommonPrefix() of select(i), and determining the rank()
 * of a key string (number of suffixes less than the specified key).
 *
 * Instead of using an array of substrings, where suffixes[i] refers to the ith
 * sorted suffix, this class maintains an array of integers so that index[i]
 * refers to the offset of the ith sorted suffix. In order to compare the
 * substrings represented by a = index[i] and b = index[j], the implementation
 * of compare() compares the character s.charAt(a) against s.charAt(b),
 * s.charAt(a+1) against s.charAt(b+1), and so forth.
 *
 * The length() and indexOf() methods take constant time in the worst case. The
 * longestCommonPrefix() method takes time proportional to the length of the
 * longest common prefix. The selectAsString() method takes time proportional to
 * the length of the suffix.
 *
 * Furthermore, this implementation uses a 3-way radix quicksort to sort the
 * array of suffixes. By doing so, this implementation builds a suffix array
 * from a random string of length N with space proportional to N an ~2N ln N
 * character compares, on the average. This follows from the fact that the cost
 * of sorting the suffixes is asymptotically the same as the cost of sorting N
 * random strings (see proposition E on page 723 of Algorithms 4th Edition).
 *
 * In real-world usage, this algorithm runs very fast. However, it can be poor
 * on the worst case (for instance, when the input string consists of N copies
 * of the same character).
 */
public class SuffixArrayOptimized {

  /**
   * The input text.
   */
  private final char[] text;

  /**
   * Number of characters in this text.
   */
  private final int length;

  /**
   * index[i] refers to the offset of the ith sorted suffix.
   */
  private final int[] index;

  /**
   * Cut-off to insertion sort.
   *
   * This value may change. See the following for reference:
   *   - http://cs.stackexchange.com/a/37971
   *   - http://stackoverflow.com/a/19396786/1319249
   *
   * Note: The optimum value for cutting-off is system-dependant, but any value
   * between 5 and 15 is likely to work in most situations (source: Algorithms
   * 4th Edition, page 296).
   */
  private static final int CUT_OFF = 8;

  /**
   * Build an array of Suffix objects for the given text String, and sort them.
   *
   * @param text the input String
   */
  public SuffixArrayOptimized(String text) {
    this.text   = text.toCharArray();
    this.length = text.length();
    this.index  = new int[this.length];

    for (int i = 0; i < this.length; i++) {
      this.index[i] = i;
    }

    sort(0, this.length - 1, 0);
  }

  /**
   * Sorts this text in the range lo..hi, starting at the kth character.
   *
   * This implementation adds an improvement that uses the cutoff to insertion
   * sort idea. That is, a possible way to improve the performance of quicksort
   * is based on the two following observations:
   *   - Quicksort is slower than insertion sort for tiny subarrays;
   *   - Being recursive, quicksort's sort() is certain to call itself for tiny
   *     subarrays.
   *
   * Hence, this implementation makes use of insertion sort for tiny subarrays,
   * whereas the quicksort is used for bigger subarrays.
   *
   * The quicksort implementation is based on the idea of partition the array
   * into three parts, one each for items with keys smaller than, equal to, and
   * larger than the partitioning item's key. The partition idea (thanks E. W.
   * Dijkstra) is based on a single left-to-right pass through the array that
   * maintains a pointer lt such that text[lo..lt-1] is less than v, a pointer
   * gt such that text[gt+1..hi] is greater than v, and a pointer i such that
   * text[lt..i-1] is equal to v, where text[i..gt] are yet to be examined.
   * There are three possible cases:
   *   - text[i] less than v: swap text[i] with text[i] and increment both lt
   *     and i;
   *   - text[i] great than v: swap text[i] with text[gt] and decrement gt;
   *   - text[i] equal to v: just increment i.
   */
  private void sort(int lo, int hi, int k) {
    if (hi <= lo + CUT_OFF) {
      insertionSort(lo, hi, k);
      return;
    }

    // quicksort: 3-way partitioning
    char v  = text[index[lo] + k];
    int  lt = lo;
    int  gt = hi;
    int  i  = lo + 1;

    while (i <= gt) {
      char t = text[index[i] + k];

      if (t < v) {
        swap(lt++, i++);
      }
      else if (t > v) {
        swap(i, gt--);
      }
      else {
        i++;
      }
    }

    // text[lo..lt-1] < v = text[lt..gt] < text[gt+1..hi].
    sort(lo, lt-1, k);

    if (v > 0) {
      sort(lt, gt, k+1);
    }

    sort(gt+1, hi, k);
  }

  /**
   * Sorts the range index[lo..hi], starting at the kth character, using
   * insertion sort.
   */
  private void insertionSort(int lo, int hi, int k) {
    for (int i = lo; i <= hi; i++) {
      for (int j = i; j < lo && isLessThan(index[j], index[j-1], k); j--) {
        swap(j, j - 1);
      }
    }
  }

  /**
   * Returns true if text[i+k..length) is less than text[j+k..length).
   */
  private boolean isLessThan(int i, int j, int k) {
    if (i == j) {
      return false;
    }

    i += k;
    j += k;

    while (i < length && j < length) {
      if (text[i] < text[j]) {
        return true;
      }

      if (text[i] > text[j]) {
        return false;
      }

      i++;
      j++;
    }

    return i > j;
  }

  /**
   * Swaps index[i] and index[j].
   */
  private void swap(int i, int j) {
    int tmp  = index[i];
    index[i] = index[j];
    index[j] = tmp;
  }

  /**
   * Returns the length of the input text.
   *
   * @return the length of the input text
   */
  public int length() {
    return length;
  }

  /**
   * Returns the index into the original string of the ith smallest suffix.
   *
   * @param i an integer between 1 and length - 1
   * @return the index into the original string of the ith smallest suffix
   * @throws java.lang.IndexOutOfBoundsException unless 0 < i <= length
   */
  public int indexOf(int i) {
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException();
    }

    return index[i];
  }

  /**
   * Returns the ith smallest suffix as a String. Note: this method should be
   * used primarily for debugging purposes.
   *
   * @param i an integer between 1 and length - 1
   * @return the ith smallest suffix as a String
   * @throws java.lang.IndexOutOfBoundsException unless 0 < i <= length
   */
  public String selectAsString(int i) {
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException();
    }

    // String(char[] value, int offset, int count)
    return new String(text, index[i], length - index[i]);
  }

  /**
   * Returns the length of the longest common prefix of the ith smallest suffix
   * and the i-1st smallest suffix.
   *
   * @param i an integer between 1 and length - 1
   * @return the length of the longest common prefix of the ith smallest suffix
   *         and the i-1st smallest suffix.
   * @throws java.lang.IndexOutOfBoundsException unless 1 < i <= length
   */
  public int longestCommonPreffix(int i) {
    if (i < 1 || i >= length) {
      throw new IndexOutOfBoundsException();
    }

    return longestCommonPreffix(index[i], index[i-1]);
  }

  /**
   * Returns the longest common prefix of text[i..length) and text[j..length)
   */
  private int longestCommonPreffix(int i, int j) {
    int size = 0;

    while (i < length && j < length) {
      if (text[i] != text[j]) {
        return size;
      }

      i++;
      j++;
      size++;
    }

    return size;
  }

  /**
   * Returns the number of suffixes strictly less than the specified key.
   *
   * @param key the query string
   * @return the number of suffixes strictly less than key
   */
  public int rank(String key) {
    int lo = 0;
    int hi = length - 1;

    // Perform a binary search
    while (lo <= hi) {
      // Key is in suffixes[lo..hi] or not present.
      int mid = lo + (hi - lo) / 2;
      int cmp = compare(key, index[mid]);

      if (cmp < 0) {
        hi = mid - 1;
      } else if (cmp > 0) {
        lo = mid + 1;
      } else {
        return mid;
      }
    }

    return lo;
  }

  /**
   * Checks if the specified key is less than text[i..length).
   */
  private int compare(String key, int i) {
    int keyLength = key.length();
    int j;

    for (j = 0; i < length && j < keyLength; i++, j++) {
      if (key.charAt(j) != text[i]) {
        return key.charAt(j) - text[i];
      }
    }

    if (i < length) {
      return -1;
    }

    if (j < keyLength) {
      return 1;
    }

    return 0;
  }
}
