(module
  (memory (export "memory") 1)
  (data (i32.const 0) "\f0\de\bc\9a\78\56\34\12") ;; 0x123456789abcdef0
  (data (i32.const 8) "\10\32\54\76\98\ba\dc\fe") ;; 0xfedcba9876543210

  (func (export "test_i64_load") (result i64)
    i32.const 0 ;; address
    i64.load offset=0 align=1
  )

  (func (export "test_i64_load_offset") (result i64)
    i32.const 0 ;; base address
    i64.load offset=8 align=1 ;; effective address 8
  )
)
