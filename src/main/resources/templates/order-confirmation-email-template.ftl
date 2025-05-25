<#ftl encoding="UTF-8">
<!DOCTYPE html>
<html lang="en">
<head>
    <style>
        .thankYouText {
            color: #f1f1f1;
        }
        h2 {
            color: #000;
        }
        h3 {
            font-size: 1.1125rem;
            color: #fbaf85;
            padding-top: 3rem;
        }
        h4 {
            color: #101010;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            padding: 10px;
            text-align: left;
        }
        th {
            background-color: #000;
            color: #d87d4a;
        }
        td {
            background-color: #f1f1f1;
            color: #000;
        }
        .total {
            font-weight: bold;
            text-align: left;
            color: #000;
        }
        .totalPrice {
            color: #000;
        }
        .thankYouBanner {
            background-color: #000;
            padding-top: 1rem;
            padding-bottom: 1rem;
            padding-left: 0.5rem;
        }
        .processedOrder {
            color: #000;
            opacity: 0.5;
            font-weight: bold;
        }
        .billingDetailsContent {
            padding-left: 0.5rem;
            color: #000;
        }
        .billingValue {
            opacity: 0.5;
        }
        .orderId {
            color: #000;
            opacity: 0.5;
        }
    </style>
</head>
<body>
<div class="thankYouBanner">
    <h2 class="thankYouText">Thank you for your order</h2>
</div>
<p class="processedOrder">Your order has been received and is now being processed. Your order details are shown below for your reference:</p>
<h3>Order Confirmation <span class="orderId">(#${order.id})</span></h3>
<table>
    <thead>
    <tr>
        <th>Name</th>
        <th>Price</th>
        <th>Quantity</th>
    </tr>
    </thead>
    <tbody>
    <#list items as item>
        <tr>
            <td>${item.product.name}</td>
            <td>${item.product.price}</td>
            <td>x${item.quantity}</td>
        </tr>
    </#list>
    </tbody>
</table>
<div class="total">
    <h2>Total Price: <span class="totalPrice">${order.total}$</span></h2>
</div>
    <h3>Billing Details</h3>
<div class="billingDetailsContent">
    <p>PHONE NUMBER <span class="billingValue">${order.billingInformation.phoneNumber}</span></p>
    <p>COUNTRY <span class="billingValue">${order.billingInformation.country}</span></p>
    <p>CITY <span class="billingValue">${order.billingInformation.city}</span></p>
    <p>ADDRESS <span class="billingValue">${order.billingInformation.address}</span></p>
    <p>ZIP CODE <span class="billingValue">${order.billingInformation.zipCode}</span></p>
    <p>BILLING OPTION <span class="billingValue">${order.billingInformation.paymentOption}</span></p>
</div>

</body>
</html>
