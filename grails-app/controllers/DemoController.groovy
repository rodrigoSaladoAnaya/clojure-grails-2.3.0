class DemoController {
    
    def simple = {
        render clj.simple()
    }
    
    def one = {
        render clj['one'].doit()
    }
    
    def two = {
        render clj['two'].doit()
    }
    
    def fibo = {
        log.info params
        def cnt = params.id?.toInteger()
        def numbers
        if(cnt) {
            numbers = clj.fibo(cnt)
        } else {
            numbers = []
        }
        render view: 'fibo', model: [numbers: numbers]
    }
}
