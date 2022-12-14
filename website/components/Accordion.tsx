import { NextPage } from "next"
import { useEffect } from "react"

class AccordionAnimationHandler {
    el: HTMLDetailsElement
    summary: HTMLElement
    content: HTMLDivElement
    chevron: SVGElement
    header: HTMLSpanElement
    headerAnimation: Animation | null
    chevronAnimation: Animation | null
    animation: Animation | null
    isClosing: boolean
    isExpanding: boolean
    animationTime: number

    constructor(el: HTMLDetailsElement, animationTime: number, identifier: string) {
        this.el = el
        this.animationTime = animationTime
        this.summary = el.querySelector(`.summary-${identifier}`) as HTMLElement
        this.content = el.querySelector(`.content-${identifier}`) as HTMLDivElement
        this.chevron = el.querySelector(`.summary-${identifier} > svg`) as SVGElement
        this.header = el.querySelector(`.summary-${identifier} span`) as HTMLSpanElement
        this.chevronAnimation = null
        this.headerAnimation = null
        this.animation = null
        this.isClosing = false
        this.isExpanding = false
        this.summary.addEventListener('click', (e) => this.onClick(e))
    }

    onClick(e: Event) {
        e.preventDefault()
        this.el.style.overflow = 'hidden'
        if (this.isClosing || !this.el.open) {
            this.open()
        } else if (this.isExpanding || this.el.open) {
            this.shrink()
        }
    }

    shrink() {
        this.isClosing = true

        const startHeight = `${this.el.offsetHeight}px`
        const endHeight = `${this.summary.offsetHeight}px`

        if (this.animation) {
            this.animation.cancel()
        }

        if (this.chevronAnimation) {
            this.chevronAnimation.cancel()
        }

        if (this.headerAnimation) {
            this.headerAnimation.cancel()
        }

        this.headerAnimation = this.header.animate({
            backgroundSize: ["100% 1.5px", "0% 1.5px"]
        }, { duration: this.animationTime })

        this.animation = this.el.animate({
            height: [startHeight, endHeight]
        }, {
            duration: this.animationTime,
            easing: 'ease-out'
        })

        this.chevronAnimation = this.chevron.animate([
            { transform: 'rotate(-180deg)' },
            { transform: 'rotate(0)' }
        ], { duration: this.animationTime, iterations: 1 })

        this.animation.onfinish = () => this.onAnimationFinish(false)
        this.animation.oncancel = () => this.isClosing = false
    }

    open() {
        this.el.style.height = `${this.el.offsetHeight}px`
        this.el.open = true
        window.requestAnimationFrame(() => this.expand())
    }

    expand() {
        this.isExpanding = true
        const startHeight = `${this.el.offsetHeight}px`
        const endHeight = `${this.summary.offsetHeight + this.content.offsetHeight}px`

        if (this.animation) {
            this.animation.cancel()
        }

        if (this.chevronAnimation) {
            this.chevronAnimation.cancel()
        }

        if (this.headerAnimation) {
            this.headerAnimation.cancel()
        }

        this.headerAnimation = this.header.animate({
            backgroundSize: ["0% 1.5px", "100% 1.5px"]
        }, { duration: this.animationTime })

        this.animation = this.el.animate({
            height: [startHeight, endHeight]
        }, {
            duration: this.animationTime,
            easing: 'ease-out'
        })

        this.chevronAnimation = this.chevron.animate([
            { transform: 'rotate(0)' },
            { transform: 'rotate(-180deg)' }
        ], { duration: this.animationTime, iterations: 1 })

        this.animation.onfinish = () => this.onAnimationFinish(true)
        this.animation.oncancel = () => this.isExpanding = false
    }

    onAnimationFinish(open: boolean) {
        this.el.open = open
        this.animation = null
        this.chevronAnimation = null
        this.headerAnimation = null
        this.isClosing = false
        this.isExpanding = false
        this.chevron.style.transform = open ? 'rotate(-180deg)' : 'rotate(0)'
        this.header.style.backgroundSize = open ? '100% 1.5px' : '0% 1.5px'
        this.el.style.height = this.el.style.overflow = ''
    }
}

type Props = {
    items: { conditionalClick?: Function, headerExtras?: React.ReactElement, heading: string, body: React.ReactElement }[],
    animationTime: number,
    loaded: boolean
}

const Accordion: NextPage<Props> = (props: Props) => {
    const alphabet = "abcdefghijklmnopqrstuvwxyz"
    let id = ""
    for (let i = 0; i < 6; i++) {
        id += alphabet[Math.floor(Math.random() * alphabet.length)]
    }

    useEffect(() => {
        document.querySelectorAll(`#${id} > div > details`).forEach((el) => {
            const element = el as HTMLDetailsElement

            new AccordionAnimationHandler(element, props.animationTime, id)
        })
    }, [props.items])

    useEffect(() => {
        let maxWidth: number = 0

        document.querySelectorAll(`#${id} > div > details`).forEach((el) => {
            const openedElements: HTMLDetailsElement[] = []
            const element = el as HTMLDetailsElement
            if (element.open) {
                openedElements.push(element)
            }
            element.open = true
            const nestedElementList: HTMLDetailsElement[] = []
            let nestedElement: HTMLDetailsElement | null = element
            do {
                nestedElement = nestedElement.querySelector("details") as HTMLDetailsElement
                if (nestedElement) {
                    if (nestedElement.open) {
                        openedElements.push(nestedElement)
                    }
                    nestedElement.open = true
                    nestedElementList.push(nestedElement)
                }
            } while (nestedElement != null)
            maxWidth = maxWidth < element.offsetWidth ? element.offsetWidth : maxWidth
            nestedElementList.forEach(element => element.removeAttribute("open"))

            el.removeAttribute("open")

            openedElements.forEach(element => {
                element.open = true
            })
        })
        const container = document.querySelector(`#${id}`) as HTMLDivElement
        if (maxWidth != 0) {
            container.style.minWidth = `${maxWidth + 1}px` // add 1 to account for decimal px
        }
    }, [props.loaded, props.items.flat().length])

    return (
        <div id={id} className="divide-black divide-opacity-40 divide-y">
            {
                props.items.map((item, index) => {
                    return (
                        <div key={index} className="mb-2 flex">
                            <div>
                                {item.headerExtras && item.headerExtras}
                            </div>
                            <details className="w-full" onClick={() => item.conditionalClick ? item.conditionalClick() : () => { }}>
                                <summary className={`summary-${id} cursor-pointer pt-2 select-none flex justify-between items-center flex-row`}>
                                    <div className="flex">
                                        <span className="fancy-underline mr-12 font-medium ">{item.heading}</span>
                                    </div>
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 mt-0.5">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
                                    </svg>
                                </summary>
                                <div className={`content-${id}`}>
                                    {item.body}
                                </div>
                            </details>
                        </div>
                    )
                })
            }
        </div >
    )
}

export default Accordion

