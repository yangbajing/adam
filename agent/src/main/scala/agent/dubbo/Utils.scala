package agent.dubbo

import akka.util.ByteString

object Utils {

  def bytes2long(b: ByteString, off: Int): Long = {
    ((b(off + 7) & 0xFFL) << 0) +
      ((b(off + 6) & 0xFFL) << 8) +
      ((b(off + 5) & 0xFFL) << 16) +
      ((b(off + 4) & 0xFFL) << 24) +
      ((b(off + 3) & 0xFFL) << 32) +
      ((b(off + 2) & 0xFFL) << 40) +
      ((b(off + 1) & 0xFFL) << 48) +
      (b(off + 0).toLong << 56)
  }

}
