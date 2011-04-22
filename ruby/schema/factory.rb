
class ModelObject
  def initialize(klass)
    @field = {}
    @klass = klass
    for each field
      init many-valued with ManyField.new
      init others with default
  end
  
  def method_missing(m, *args, &block)
    if matches = then
      is the name in the klass
      is the type right
      if its manyvalue, error
      if its optional... check that
      assign
      if its has an inverse, call it
    else
      get the name
      is the name in the klass
      get the value
    end  
  end
end

# eg. "classes" field on Schema
class ManyField
  def initialize(field)
    @hash = {}
    @field = field
    @key = field.type.key.name  # e.g. "name" field Klass
  end
  
  def [](x)
    @hash[x]
  end
  
  def length
    @hash.length
  end
  
  def keys
    @hash.keys
  end
  
  def values
    @hash.values
  end
  
  def <<(v)
    k = v.send(key)
    self[k] = v
  end
  
  def []=(k, v)
    hash[k] = v
    v.send("#{field.inverse.name}Internal=", self)
  end
  
  def deleteByKey(v)

  end
  
  def each(&block) 
    hash.each_value &block
  end
end  


; A definition of the Schema metamodel in the DSL for defining Schemas whose 
;   grammar is in Schema-SGrammar.gel

(cond-load "Support/syntax.scm")
(cond-load "mix/online.scm")
(cond-load "Schema/bootstrap.scm")


; this code defines an object implementation for instances of a schema
(mixdefine factory-code
  (define (factory schema name init)
    (for type (: (: schema 'types) 'items) first
         (if (eq? name (string->symbol (: type 'name)))
             (make type init))
         (error-msg `(Attempting to instantiate unknown type ,name within schema ,(: schema 'name)))))

  (export-define (make type init)
    ;; intialize the fields
    ;; how to store them?
    (slet (data (dynamic (create-hash '())))
      (object (this msg args)
        ; initialization
        (for field (: (: type 'fields) 'items) begin
             (if (not (: field 'computed))
                 (if (: field 'many)
                     (table-set! data (: field 'name) (collection field this))
                 ; single-valued
                 ; has explicit init
                 (if (assoc (string->symbol (: field 'name)) init)
                     (: this (make-symbol 'set- (: field 'name))
                        (cdr (assoc (string->symbol (: field 'name)) init)))
                 ; default value
                 (if (defined? (: field 'expression))
                     (: this (make-symbol 'set- (: field 'name))
                        (plugin (: field 'expression) type)))))))
        ; method execution
        (for field (: (: type 'fields) 'items) first
             ; TODO: needs to be an extension point, but otherwise should be inlined!
             (if (eq? msg (string->symbol (: field 'name)))
                 ; get the value
                 (if (: field 'computed)
                     (plugin (: field 'expression) type)
                     (table-ref data (: field 'name) (void)))
             (if (and (not (: field 'many))
                      (not (: field 'computed)))
                 ; store the value
                 (if (eq? msg (make-symbol 'primitive-set- (: field 'name)))
                     (table-set! data (: field 'name) (car args))
                 ; acces the data 
                 (if (eq? msg (make-symbol 'set- (: field 'name)))
                     (begin
                       (if (defined? (car args))
                           (if (not (is-a (car args) (: field 'type)))
                               (error-msg `(Assigment to ,(: field 'name) in ,(: type 'name) to invalid data ,data)))
                       (if (not (: field 'optional))
                           (error-msg `(Assigning required field ,(: field 'name) in ,(: type 'name) to ,(car args)))))
                       (if (not (eq? (car args) (table-ref data (: field 'name) (void))))
                           (if (defined? (: field 'inverse))
                               ; has an inverse
                               (if (: (: field 'inverse) 'many)
                                   (begin ; many-valued inverse
                                     ; TODO: there is an issue if you set the key of an object after it is inserted somewhere
                                     ; TODO: we can't assume that required fields are defined
                                     ; until the object is validated
                                     (if (defined? (table-ref data (: field 'name) (void)))
                                         ; field-v@ data 'itemsalue.(inverse.name).remove(this)
                                         (: (: (table-ref data (: field 'name)) (: (: field 'inverse) 'name)) 'primitive-remove this))
                                     (table-set! data (: field 'name) (car args))
                                     (if (if (: field 'optional)
                                             (defined? (car args))
                                             #t)
                                         ; field-value.(inverse.name).insert(this)
                                         (: (: (car args)
                                               (string->symbol (: (: field 'inverse) 'name)))
                                            'primitive-insert this)))
                                   (begin ; single-valued inverse
                                     (table-set! data (: field 'name) (car args))
                                     (if (defined? (car args))
                                         (: (car args)
                                            (make-symbol 'primitive-set- (: (: field 'inverse) 'name))
                                            this))
                                     ))
                               ; no inverse
                               (table-set! data (: field 'name) (car args)))))
                     ))
                 )) ; make sure to return a value
             ; create a full key, including our parents keys
             (if (eq? msg 'meta-type)
                 (: type 'name)
             (if (eq? msg 'single-key)
                 (for field (: (: type 'fields) 'items) append
                      (if (and (: field 'key)
                               (: (: field 'type) 'primitive))
                          (list (: this (string->symbol (: field 'name))))
                          '()))
             (if (eq? msg 'full-key)
                 (append ; note that this is really a sort function
                  (for field (: (: type 'fields) 'items) append
                       (if (and (: field 'key)
                                (not (: (: field 'type) 'primitive)))
                           (: (: this (string->symbol (: field 'name))) 'full-key)
                           '()))
                  (: this 'key))
             (if (eq? msg '*dump*)
                 (table->list data)
                 (error-msg `(Class ,(: type 'name) does not understand ,msg with args ,args (object dump ,(table->list data))))
                 ))))))))



; a "bi-directional" collection. It ensures that its contents point back at it
  (export-define (collection field owner)
    (slet (data (if (defined? (type-key (: field 'type)))
                    (make-hash-collection (: field 'type)) ; don't need dynamic... its specialized
                    (dynamic (make-list-collection '()))))
      (object (collection msg args) #t
        (if (eq? msg 'item)
            (if (null? (cdr args))
                (: data 'item (car args)) ; without index value
                (: data 'item (car args) (cadr args))) ; with index value
        (if (eq? msg 'items)
            (: data 'items)
        (if (eq? msg 'size)
            (: data 'size)
        (if (eq? msg 'insert)
            (if (not (is-a (car args) (: field 'type)))
                (error-msg `(Incorrect type of value ,(car args) inserted into ,(: field 'name) of type ,(: (: field 'owner) 'name)
                                       expected ,(:map 'name (all-subtypes (: field 'type)))))
            ; either set the inverse single-valued field, or just do an insert
            (if (defined? (: field 'inverse))
                ; if it has an inverse, just tell the value to change its pointer.. its easier that way
                (: (car args) (make-symbol 'set- (: (: field 'inverse) 'name)) owner)
                ; otherwise, just do a primitive insert
                (: data 'insert (car args))))
        (if (eq? msg 'contains)
            (if (not (is-a (car args) (: field 'type)))
                (error-msg `(Testing value ,(car args) of wrong type for ,(: field 'name) of type ,(: (: field 'owner) 'name)
                                     expected ,(:map 'name (all-subtypes (: field 'type)))))
                (data 'contains (car args)))
        (if (eq? msg 'set-item)
            (if (not (is-a (car args) (: field 'type)))
                (error-msg `(Incorrect type of value ,(car args) set for ,(: field 'name) of type ,(: (: field 'owner) 'name)
                                       expected ,(:map 'name (all-subtypes (: field 'type)))))
            ; either set the inverse single-valued field, or just do an insert
            (if (defined? (: field 'inverse))
                ; if it has an inverse, just tell the value to change its pointer.. its easier that way
                (: (car args) (make-symbol 'set- (: (: field 'inverse) 'name)) owner)
                ; otherwise, just do a primitive insert
                (: data 'set-item (car args))))
        (if (eq? msg 'remove)
            ; either clear the inverse single-valued field, or just remove it
            ; TODO: check that its in the collection!!!
            (if (defined? (: field 'inverse))
                ; if it has an inverse, just tell the value to change its pointer.. its easier that way
                (: (car args) (make-symbol 'set- (: (: field 'inverse) 'name)) (void))
                ; otherwise, just do a primitive insert
                (: data 'remove (car args)))
        (if (eq? msg 'primitive-insert)
            (: data 'insert (car args))
        (if (eq? msg 'primitive-remove)
            (: data 'remove (car args))
        (if (eq? msg '*dump*)
            (: data '*dump*)
	(if (eq? msg 'meta-type)
	    "collection"
            (error-msg `(Collection ,(: field 'name) owned by ,owner does not understand (,msg ,args)))
            ))))))))))))))
