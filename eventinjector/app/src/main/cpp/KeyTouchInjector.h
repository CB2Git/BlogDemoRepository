#ifndef _KEY_TOUCH_H_
#define _KEY_TOUCH_H_

#ifndef _TEST_BIT_
#define _TEST_BIT_
#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))
#endif

#endif